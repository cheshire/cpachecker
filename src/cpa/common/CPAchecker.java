/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker. 
 *
 *  Copyright (C) 2007-2008  Dirk Beyer and Erkan Keremoglu.
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *  CPAchecker web page:
 *    http://www.cs.sfu.ca/~dbeyer/CPAchecker/
 */
package cpa.common;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.cdt.core.dom.IASTServiceProvider;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.IParserConfiguration;
import org.eclipse.cdt.core.dom.IASTServiceProvider.UnsupportedDialectException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.InternalASTServiceProvider;
import org.eclipse.core.resources.IFile;

import cfa.CFABuilder;
import cfa.CFACheck;
import cfa.CFAReduction;
import cfa.CFASimplifier;
import cfa.CFATopologicalSort;
import cfa.CPASecondPassBuilder;
import cfa.DOTBuilder;
import cfa.objectmodel.BlankEdge;
import cfa.objectmodel.CFAEdge;
import cfa.objectmodel.CFAFunctionDefinitionNode;
import cfa.objectmodel.CFANode;
import cfa.objectmodel.c.GlobalDeclarationEdge;
import cmdline.stubs.StubCodeReaderFactory;
import cmdline.stubs.StubConfiguration;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import common.Pair;
import common.configuration.Configuration;
import common.configuration.Option;
import common.configuration.Options;

import cpa.common.CPAcheckerResult.Result;
import cpa.common.algorithm.Algorithm;
import cpa.common.algorithm.AssumptionCollectionAlgorithm;
import cpa.common.algorithm.CBMCAlgorithm;
import cpa.common.algorithm.CEGARAlgorithm;
import cpa.common.algorithm.CPAAlgorithm;
import cpa.common.interfaces.AbstractElement;
import cpa.common.interfaces.ConfigurableProgramAnalysis;
import cpa.common.interfaces.Precision;
import cpa.common.interfaces.StatisticsProvider;
import exceptions.CFAGenerationRuntimeException;
import exceptions.CPAException;
import exceptions.ForceStopCPAException;
import exceptions.InvalidConfigurationException;

@SuppressWarnings("restriction")
public class CPAchecker {
  
  @Options
  private static class CPAcheckerOptions {

    @Option(name="parser.dialect", toUppercase=true, values={"C99", "GNUC"})
    String parserDialect = "GNUC";

    // CFA creation and initialization options
    
    @Option(name="analysis.entryFunction", regexp="^[_a-zA-Z][_a-zA-Z0-9]*$")
    String mainFunctionName = "main";
    
    @Option(name="cfa.combineBlockStatements")
    boolean combineBlockStatements = false;
    
    @Option(name="cfa.removeDeclarations")
    boolean removeDeclarations = false;
    
    @Option(name="analysis.noExternalCalls")
    boolean noExternalCalls = true;
    
    @Option(name="analysis.interprocedural")
    boolean interprocedural = true;
    
    @Option(name="analysis.useGlobalVars")
    boolean useGlobalVars = true;
    
    @Option(name="cfa.removeIrrelevantForErrorLocations")
    boolean removeIrrelevantForErrorLocations = false;

    @Option(name="cfa.export")
    boolean exportCfa = true;
    
    @Option(name="cfa.file")
    String exportCfaFile = "cfa.dot";
    
    @Option(name="output.path")
    String outputDirectory = "test/output/";
    
    // algorithm options
    
    @Option(name="analysis.traversal")
    ReachedElements.TraversalMethod traversalMethod = ReachedElements.TraversalMethod.DFS;
    
    @Option(name="cpa.useSpecializedReachedSet")
    boolean locationMappedReachedSet = true;
    
    @Option(name="analysis.useAssumptionCollector")
    boolean useAssumptionCollector = false;
    
    @Option(name="analysis.useRefinement")
    boolean useRefinement = false;
    
    @Option(name="analysis.useCBMC")
    boolean useCBMC = false;

    @Option(name="analysis.stopAfterError")
    boolean stopAfterError = true;
    
  }
  
  private final Configuration config;
  private final CPAcheckerOptions options;

  // TODO these fields should not be public and static
  // Write access to these fields is prohibited from outside of this class!
  // Use the constructor to initialize them.
  public static LogManager logger = null;
  
  private static volatile boolean requireStopAsap = false;
  
  /**
   * This method will throw an exception if the user has requested CPAchecker to
   * stop immediately. This exception should not be caught by the caller.
   */
  public static void stopIfNecessary() throws ForceStopCPAException {
    if (requireStopAsap) {
      throw new ForceStopCPAException();
    }
  }

  /**
   * This will request all running CPAchecker instances to stop as soon as possible.
   */
  public static void requireStopAsap() {
    requireStopAsap = true;
  }
  
  public CPAchecker(Configuration pConfiguration, LogManager pLogManager) throws InvalidConfigurationException {
    // currently only one instance is possible due to these static fields 
    assert logger == null;
    
    config = pConfiguration;
    logger = pLogManager;

    options = new CPAcheckerOptions();
    config.inject(options);
  }
  
  protected Configuration getConfiguration() {
    return config;
  }
  
  public CPAcheckerResult run(IFile file) {
    
    logger.log(Level.FINE, "Analysis Started");
    
    MainCPAStatistics stats = null;
    ReachedElements reached = null;
    Result result = Result.UNKNOWN;
    
    try {
      // parse code file
      IASTTranslationUnit ast = parse(file);
      if (ast == null) {
        // parsing failed
        return new CPAcheckerResult(Result.UNKNOWN, null, null);
      }

      stats = new MainCPAStatistics(getConfiguration(), logger);

      // start measuring time
      stats.startProgramTimer();
  
      // create CFA
      Pair<Map<String, CFAFunctionDefinitionNode>, CFAFunctionDefinitionNode> cfa = createCFA(ast);
      if (cfa == null) {
        // empty program, do nothing
        return new CPAcheckerResult(Result.UNKNOWN, null, null);
      }
      
      Map<String, CFAFunctionDefinitionNode> cfas = cfa.getFirst();
      CFAFunctionDefinitionNode mainFunction = cfa.getSecond();
    
      ConfigurableProgramAnalysis cpa = createCPA(stats);
      
      Algorithm algorithm = createAlgorithm(cfas, cpa, stats);
      
      Set<String> unusedProperties = getConfiguration().getUnusedProperties();
      if (!unusedProperties.isEmpty()) {
        logger.log(Level.WARNING, "The following configuration options were specified but are not used:\n",
            Joiner.on("\n ").join(unusedProperties), "\n");
      }
      
      if (!requireStopAsap) {
        reached = createInitialReachedSet(cpa, mainFunction);
      
        result = runAlgorithm(algorithm, reached, stats);
      }
    
    } catch (CFAGenerationRuntimeException e) {
      // only log message, not whole exception because this is a C problem,
      // not a CPAchecker problem
      logger.log(Level.SEVERE, e.getMessage());
      logger.log(Level.INFO, "Make sure that the code was preprocessed using Cil (HowTo.txt).\n"
          + "If the error still occurs, please send this error message together with the input file to cpachecker-users@sosy-lab.org.");
      
    } catch (InvalidConfigurationException e) {
      logger.log(Level.SEVERE, "Invalid configuration:", e.getMessage());
      
    } catch (ForceStopCPAException e) {
      // CPA must exit because it was asked to
      logger.log(Level.FINE, "ForceStopCPAException caught at top level: CPAchecker has stopped forcefully, but cleanly");
    } catch (CPAException e) {
      logger.logException(Level.SEVERE, e, null);
    }
    
    return new CPAcheckerResult(result, reached, stats);
  }
  
  /**
   * Parse the content of a file into an AST with the Eclipse CDT parser.
   * If an error occurs, the program is halted.
   * 
   * @param fileName  The file to parse.
   * @return The AST.
   * @throws InvalidConfigurationException 
   */
  public IASTTranslationUnit parse(IFile file) throws InvalidConfigurationException {
    IASTServiceProvider p = new InternalASTServiceProvider();
    
    ICodeReaderFactory codeReaderFactory = new StubCodeReaderFactory();
    IParserConfiguration parserConfiguration = new StubConfiguration(options.parserDialect);
    
    IASTTranslationUnit ast = null;
    try {
      ast = p.getTranslationUnit(file, codeReaderFactory, parserConfiguration);
    } catch (UnsupportedDialectException e) {
      // should never occur here because the value of the option is checked before 
      throw new InvalidConfigurationException("Unsupported C dialect " + options.parserDialect);
    }

    logger.log(Level.FINE, "Parser Finished");

    return ast;
  }
  
  /**
   * --Refactoring:
   * Initializes the CFA. This method is created based on the 
   * "extract method refactoring technique" to help simplify the createCFA method body.
   * @param builder
   * @param cfas
   * @return
   * @throws InvalidConfigurationException 
   */
  private CFAFunctionDefinitionNode initCFA(final CFABuilder builder, final Map<String, CFAFunctionDefinitionNode> cfas) throws InvalidConfigurationException
  {
    CFAFunctionDefinitionNode mainFunction = cfas.get(options.mainFunctionName);
    
    if (mainFunction == null) {
      throw new InvalidConfigurationException("Function " + options.mainFunctionName + " not found!");
    }

    // Insert call and return edges and build the supergraph
    if (options.interprocedural) {
      logger.log(Level.FINE, "Analysis is interprocedural, adding super edges");
      
      CPASecondPassBuilder spbuilder = new CPASecondPassBuilder(cfas, options.noExternalCalls);
      Set<String> calledFunctions = spbuilder.insertCallEdgesRecursively(options.mainFunctionName);
      
      // remove all functions which are never reached from cfas
      cfas.keySet().retainAll(calledFunctions);
    }
    
    if (options.useGlobalVars){
      // add global variables at the beginning of main
      
      List<IASTDeclaration> globalVars = builder.getGlobalDeclarations();
      insertGlobalDeclarations(mainFunction, globalVars);
    }
    
    // simplify CFA
    if (options.combineBlockStatements || options.removeDeclarations) {
      CFASimplifier simplifier = new CFASimplifier(options.combineBlockStatements, options.removeDeclarations);
      simplifier.simplify(mainFunction);
    }
    
    return mainFunction;
  }
  
  
  protected Pair<Map<String, CFAFunctionDefinitionNode>, CFAFunctionDefinitionNode> createCFA(IASTTranslationUnit ast) throws InvalidConfigurationException, CFAGenerationRuntimeException {

    // Build CFA
    final CFABuilder builder = new CFABuilder(logger);
    ast.accept(builder);

    final Map<String, CFAFunctionDefinitionNode> cfas = builder.getCFAs();
    
    // annotate CFA nodes with topological information for later use
    for(CFAFunctionDefinitionNode cfa : cfas.values()){
      CFATopologicalSort topSort = new CFATopologicalSort();
      topSort.topologicalSort(cfa);
    }
    
    // --Refactoring:
    CFAFunctionDefinitionNode mainFunction = initCFA(builder, cfas);
    
    // --Refactoring: The following commented section does not affect the actual 
    //                execution of the code
    
    // check the CFA of each function
    for (CFAFunctionDefinitionNode cfa : cfas.values()) {
      assert CFACheck.check(cfa);
    }

    // --Refactoring: The following section was relocated to after the "initCFA" method 
    
    // remove irrelevant locations
    if (options.removeIrrelevantForErrorLocations) {
      CFAReduction coi =  new CFAReduction();
      coi.removeIrrelevantForErrorLocations(mainFunction);

      if (mainFunction.getNumLeavingEdges() == 0) {
        logger.log(Level.INFO, "No error locations reachable from " + mainFunction.getFunctionName()
              + ", analysis not necessary.");
        return null;
      }
    }
    
    // check the super CFA starting at the main function
    assert CFACheck.check(mainFunction);

    // write CFA to file
    if (options.exportCfa) {
      DOTBuilder dotBuilder = new DOTBuilder();
      File cfaFile = new File(options.outputDirectory, options.exportCfaFile);
      
      try {
        dotBuilder.generateDOT(cfas.values(), mainFunction, cfaFile);
      } catch (IOException e) {
        logger.log(Level.WARNING,
          "Could not write CFA to dot file, check configuration option cfa.file! (",
          e.getMessage() + ")");
        // continue with analysis
      }
    }
    
    logger.log(Level.FINE, "DONE, CFA for", cfas.size(), "functions created");

    return new Pair<Map<String, CFAFunctionDefinitionNode>, CFAFunctionDefinitionNode>(
        ImmutableMap.copyOf(cfas), mainFunction);
  }

  /**
   * Insert nodes for global declarations after first node of CFA.
   */
  private void insertGlobalDeclarations(
      final CFAFunctionDefinitionNode cfa, List<IASTDeclaration> globalVars) {
    if (globalVars.isEmpty()) {
      return;
    }
    // create a series of GlobalDeclarationEdges, one for each declaration,
    // and add them as successors of the input node
    List<CFANode> decls = new LinkedList<CFANode>();
    CFANode cur = new CFANode(0);
    cur.setFunctionName(cfa.getFunctionName());
    decls.add(cur);

    for (IASTDeclaration d : globalVars) {
      assert(d instanceof IASTSimpleDeclaration);
      IASTSimpleDeclaration sd = (IASTSimpleDeclaration)d;
      // TODO refactor this
//      if (sd.getDeclarators().length == 1 &&
//          sd.getDeclarators()[0] instanceof IASTFunctionDeclarator) {
//        if (cpaConfig.getBooleanValue("analysis.useFunctionDeclarations")) {
//          // do nothing
//        }
//        else {
//          System.out.println(d.getRawSignature());
//          continue;
//        }
//      }
      CFANode n = new CFANode(sd.getFileLocation().getStartingLineNumber());
      n.setFunctionName(cur.getFunctionName());
      GlobalDeclarationEdge e = new GlobalDeclarationEdge(
          d.getRawSignature(), sd.getFileLocation().getStartingLineNumber(), cur, n,
          sd.getDeclarators(),
          sd.getDeclSpecifier());
      e.addToCFA();
      decls.add(n);
      cur = n;
    }

    // split off first node of CFA
    assert cfa.getNumLeavingEdges() == 1;
    assert cfa.getLeavingSummaryEdge() == null;
    CFAEdge firstEdge = cfa.getLeavingEdge(0);
    assert firstEdge instanceof BlankEdge && !firstEdge.isJumpEdge();
    CFANode secondNode = firstEdge.getSuccessor();
    
    cfa.removeLeavingEdge(firstEdge);
    secondNode.removeEnteringEdge(firstEdge);
    
    // and add a blank edge connecting the first node of CFA with declarations
    BlankEdge be = new BlankEdge("INIT GLOBAL VARS", 0, cfa, decls.get(0));
    be.addToCFA();

    // and a blank edge connecting the declarations with the second node of CFA
    be = new BlankEdge(firstEdge.getRawStatement(), firstEdge.getLineNumber(), cur, secondNode);
    be.addToCFA();
    
    return;
  }
  
  private Result runAlgorithm(final Algorithm algorithm,
          final ReachedElements reached,
          final MainCPAStatistics stats) throws CPAException {

    logger.log(Level.INFO, "Starting analysis...");
    stats.startAnalysisTimer();
    
    algorithm.run(reached, options.stopAfterError);
    
    stats.stopAnalysisTimer();
    logger.log(Level.INFO, "Analysis finished.");

    for (AbstractElement reachedElement : reached) {
      if (reachedElement.isError()) {
        return Result.UNSAFE;
      }
    }

    return Result.SAFE;
  }

  private ConfigurableProgramAnalysis createCPA(MainCPAStatistics stats) throws CPAException {
    logger.log(Level.FINE, "Creating CPAs");
    
    CPABuilder builder = new CPABuilder(getConfiguration(), logger);
    ConfigurableProgramAnalysis cpa = builder.buildCPAs();

    if (cpa instanceof StatisticsProvider) {
      ((StatisticsProvider)cpa).collectStatistics(stats.getSubStatistics());
    }
    return cpa;
  }
  
  private Algorithm createAlgorithm(final Map<String, CFAFunctionDefinitionNode> cfas,
      final ConfigurableProgramAnalysis cpa, MainCPAStatistics stats) throws CPAException {
    logger.log(Level.FINE, "Creating algorithms");

    Algorithm algorithm = new CPAAlgorithm(cpa, logger);
    
    if (options.useRefinement) {
      algorithm = new CEGARAlgorithm(algorithm, getConfiguration(), logger);
    }
    
    if (options.useAssumptionCollector) {
      algorithm = new AssumptionCollectionAlgorithm(algorithm, getConfiguration(), logger);
    }
    
    if (options.useCBMC) {
      algorithm = new CBMCAlgorithm(cfas, algorithm);
    }
    
    if (algorithm instanceof StatisticsProvider) {
      ((StatisticsProvider)algorithm).collectStatistics(stats.getSubStatistics());
    }
    return algorithm;
  }


  private ReachedElements createInitialReachedSet(
      final ConfigurableProgramAnalysis cpa,
      final CFAFunctionDefinitionNode mainFunction) {
    logger.log(Level.FINE, "Creating initial reached set");
    
    AbstractElement initialElement = cpa.getInitialElement(mainFunction);
    Precision initialPrecision = cpa.getInitialPrecision(mainFunction);
    ReachedElements reached = new ReachedElements(options.traversalMethod, options.locationMappedReachedSet);
    reached.add(initialElement, initialPrecision);
    return reached;
  }
}
