/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2010  Dirk Beyer
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
 *    http://cpachecker.sosy-lab.org
 */
package org.sosy_lab.cpachecker.cfa.parser.eclipse;

import static java.lang.Character.isDigit;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sosy_lab.common.Pair;
import org.sosy_lab.common.Triple;
import org.sosy_lab.cpachecker.cfa.ast.BasicType;
import org.sosy_lab.cpachecker.cfa.ast.DummyType;
import org.sosy_lab.cpachecker.cfa.ast.IASTArraySubscriptExpression;
import org.sosy_lab.cpachecker.cfa.ast.IASTArrayTypeSpecifier;
import org.sosy_lab.cpachecker.cfa.ast.IASTBinaryExpression;
import org.sosy_lab.cpachecker.cfa.ast.IASTBinaryExpression.BinaryOperator;
import org.sosy_lab.cpachecker.cfa.ast.IASTCastExpression;
import org.sosy_lab.cpachecker.cfa.ast.IASTCharLiteralExpression;
import org.sosy_lab.cpachecker.cfa.ast.IASTCompositeTypeSpecifier;
import org.sosy_lab.cpachecker.cfa.ast.IASTDeclaration;
import org.sosy_lab.cpachecker.cfa.ast.IASTElaboratedTypeSpecifier;
import org.sosy_lab.cpachecker.cfa.ast.IASTEnumerationSpecifier;
import org.sosy_lab.cpachecker.cfa.ast.IASTExpression;
import org.sosy_lab.cpachecker.cfa.ast.IASTExpressionStatement;
import org.sosy_lab.cpachecker.cfa.ast.IASTFieldReference;
import org.sosy_lab.cpachecker.cfa.ast.IASTFileLocation;
import org.sosy_lab.cpachecker.cfa.ast.IASTFunctionCallExpression;
import org.sosy_lab.cpachecker.cfa.ast.IASTFunctionDefinition;
import org.sosy_lab.cpachecker.cfa.ast.IASTFunctionTypeSpecifier;
import org.sosy_lab.cpachecker.cfa.ast.IASTIdExpression;
import org.sosy_lab.cpachecker.cfa.ast.IASTInitializer;
import org.sosy_lab.cpachecker.cfa.ast.IASTInitializerExpression;
import org.sosy_lab.cpachecker.cfa.ast.IASTInitializerList;
import org.sosy_lab.cpachecker.cfa.ast.IASTIntegerLiteralExpression;
import org.sosy_lab.cpachecker.cfa.ast.IASTLiteralExpression;
import org.sosy_lab.cpachecker.cfa.ast.IASTName;
import org.sosy_lab.cpachecker.cfa.ast.IASTNamedTypeSpecifier;
import org.sosy_lab.cpachecker.cfa.ast.IASTPointerTypeSpecifier;
import org.sosy_lab.cpachecker.cfa.ast.IASTReturnStatement;
import org.sosy_lab.cpachecker.cfa.ast.IASTSimpleDeclSpecifier;
import org.sosy_lab.cpachecker.cfa.ast.IASTSimpleDeclaration;
import org.sosy_lab.cpachecker.cfa.ast.IASTStatement;
import org.sosy_lab.cpachecker.cfa.ast.IASTTypeId;
import org.sosy_lab.cpachecker.cfa.ast.IASTTypeIdExpression;
import org.sosy_lab.cpachecker.cfa.ast.IASTUnaryExpression;
import org.sosy_lab.cpachecker.cfa.ast.IComplexType;
import org.sosy_lab.cpachecker.cfa.ast.IType;
import org.sosy_lab.cpachecker.cfa.ast.ITypedef;
import org.sosy_lab.cpachecker.cfa.ast.StorageClass;
import org.sosy_lab.cpachecker.cfa.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.sosy_lab.cpachecker.cfa.ast.IASTUnaryExpression.UnaryOperator;

class ASTConverter {
    
  private static void check(boolean assertion, String msg, org.eclipse.cdt.core.dom.ast.IASTNode astNode) throws CFAGenerationRuntimeException {
    if (!assertion) {
      throw new CFAGenerationRuntimeException(msg, astNode);
    }
  }

  public static IASTExpression convert(org.eclipse.cdt.core.dom.ast.IASTExpression e) {
    assert !(e instanceof IASTExpression);

    if (e == null) {
      return null;
    
    } else if (e instanceof org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression) {
      return convert((org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression)e);
      
    } else if (e instanceof org.eclipse.cdt.core.dom.ast.IASTBinaryExpression) {
      return convert((org.eclipse.cdt.core.dom.ast.IASTBinaryExpression)e);
      
    } else if (e instanceof org.eclipse.cdt.core.dom.ast.IASTCastExpression) {
      return convert((org.eclipse.cdt.core.dom.ast.IASTCastExpression)e);
      
    } else if (e instanceof org.eclipse.cdt.core.dom.ast.IASTFieldReference) {
      return convert((org.eclipse.cdt.core.dom.ast.IASTFieldReference)e);
            
    } else if (e instanceof org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression) {
      return convert((org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression)e);
      
    } else if (e instanceof org.eclipse.cdt.core.dom.ast.IASTIdExpression) {
      return convert((org.eclipse.cdt.core.dom.ast.IASTIdExpression)e);
      
    } else if (e instanceof org.eclipse.cdt.core.dom.ast.IASTLiteralExpression) {
      return convert((org.eclipse.cdt.core.dom.ast.IASTLiteralExpression)e);
      
    } else if (e instanceof org.eclipse.cdt.core.dom.ast.IASTUnaryExpression) {
      return convert((org.eclipse.cdt.core.dom.ast.IASTUnaryExpression)e);
      
    } else if (e instanceof org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression) {
      return convert((org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression)e);
        
    } else {
      throw new CFAGenerationRuntimeException("", e);
    }
  }
  
  private static IASTArraySubscriptExpression convert(org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression e) {
    return new IASTArraySubscriptExpression(e.getRawSignature(), convert(e.getFileLocation()), convert(e.getExpressionType()), convert(e.getArrayExpression()), convert(e.getSubscriptExpression()));
  }

  private static IASTBinaryExpression convert(org.eclipse.cdt.core.dom.ast.IASTBinaryExpression e) {
    return new IASTBinaryExpression(e.getRawSignature(), convert(e.getFileLocation()), convert(e.getExpressionType()), convert(e.getOperand1()), convert(e.getOperand2()), convertBinaryOperator(e));
  }
  
  private static BinaryOperator convertBinaryOperator(org.eclipse.cdt.core.dom.ast.IASTBinaryExpression e) {
    switch (e.getOperator()) {
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_multiply:
      return BinaryOperator.MULTIPLY;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_divide:
      return BinaryOperator.DIVIDE;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_modulo:
      return BinaryOperator.MODULO;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_plus:
      return BinaryOperator.PLUS;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_minus:
      return BinaryOperator.MINUS;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_shiftLeft:
      return BinaryOperator.SHIFT_LEFT;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_shiftRight:
      return BinaryOperator.SHIFT_RIGHT;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_lessThan:
      return BinaryOperator.LESS_THAN;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_greaterThan:
      return BinaryOperator.GREATER_THAN;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_lessEqual:
      return BinaryOperator.LESS_EQUAL;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_greaterEqual:
      return BinaryOperator.GREATER_EQUAL;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_binaryAnd:
      return BinaryOperator.BINARY_AND;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_binaryXor:
      return BinaryOperator.BINARY_XOR;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_binaryOr:
      return BinaryOperator.BINARY_OR;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_logicalAnd:
      return BinaryOperator.LOGICAL_AND;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_logicalOr:
      return BinaryOperator.LOGICAL_OR;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_assign:
      return BinaryOperator.ASSIGN;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_multiplyAssign:
      return BinaryOperator.MULTIPLY_ASSIGN;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_divideAssign:
      return BinaryOperator.DIVIDE_ASSIGN;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_moduloAssign:
      return BinaryOperator.MODULO_ASSIGN;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_plusAssign:
      return BinaryOperator.PLUS_ASSIGN;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_minusAssign:
      return BinaryOperator.MINUS_ASSIGN;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_shiftLeftAssign:
      return BinaryOperator.SHIFT_LEFT_ASSIGN;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_shiftRightAssign:
      return BinaryOperator.SHIFT_RIGHT_ASSIGN;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_binaryAndAssign:
      return BinaryOperator.BINARY_AND_ASSIGN;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_binaryXorAssign:
      return BinaryOperator.BINARY_XOR_ASSIGN;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_binaryOrAssign:
      return BinaryOperator.BINARY_OR_ASSIGN;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_equals:
      return BinaryOperator.EQUALS;
    case org.eclipse.cdt.core.dom.ast.IASTBinaryExpression.op_notequals:
      return BinaryOperator.NOT_EQUALS;
    default:
      throw new CFAGenerationRuntimeException("Unknown binary operator", e);
    }
  }
  
  private static IASTCastExpression convert(org.eclipse.cdt.core.dom.ast.IASTCastExpression e) {
    return new IASTCastExpression(e.getRawSignature(), convert(e.getFileLocation()), convert(e.getExpressionType()), convert(e.getOperand()), convert(e.getTypeId()));
  }
  
  private static IASTFieldReference convert(org.eclipse.cdt.core.dom.ast.IASTFieldReference e) {
    return new IASTFieldReference(e.getRawSignature(), convert(e.getFileLocation()), convert(e.getExpressionType()), convert(e.getFieldName()), convert(e.getFieldOwner()), e.isPointerDereference());
  }
  
  private static IASTFunctionCallExpression convert(org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression e) {
    org.eclipse.cdt.core.dom.ast.IASTExpression p = e.getParameterExpression();

    List<IASTExpression> params;
    if (p instanceof org.eclipse.cdt.core.dom.ast.IASTExpressionList) {
      params = convert((org.eclipse.cdt.core.dom.ast.IASTExpressionList)p);
    
    } else {
      params = new ArrayList<IASTExpression>();
      if (p != null) {
        params.add(convert(p));
      }
    }
    
    return new IASTFunctionCallExpression(e.getRawSignature(), convert(e.getFileLocation()), convert(e.getExpressionType()), convert(e.getFunctionNameExpression()), params);
  }
  
  private static List<IASTExpression> convert(org.eclipse.cdt.core.dom.ast.IASTExpressionList es) {
    List<IASTExpression> result = new ArrayList<IASTExpression>(es.getExpressions().length);
    for (org.eclipse.cdt.core.dom.ast.IASTExpression expression : es.getExpressions()) {
      result.add(convert(expression));
    }
    return result;
  }
  
  private static IASTIdExpression convert(org.eclipse.cdt.core.dom.ast.IASTIdExpression e) {
    return new IASTIdExpression(e.getRawSignature(), convert(e.getFileLocation()), convert(e.getExpressionType()), convert(e.getName()));
  }
  
  private static IASTLiteralExpression convert(org.eclipse.cdt.core.dom.ast.IASTLiteralExpression e) {
    switch (e.getKind()) {
    case org.eclipse.cdt.core.dom.ast.IASTLiteralExpression.lk_char_constant: 
      check(e.getRawSignature().equals(String.valueOf(e.getValue())), "raw signature and value not equal", e);
      return new IASTCharLiteralExpression(e.getRawSignature(), convert(e.getFileLocation()), convert(e.getExpressionType()), parseCharacterLiteral(e.getRawSignature(), e));
    
    case org.eclipse.cdt.core.dom.ast.IASTLiteralExpression.lk_integer_constant:
      check(e.getRawSignature().equals(String.valueOf(e.getValue())), "raw signature and value not equal", e);
      return new IASTIntegerLiteralExpression(e.getRawSignature(), convert(e.getFileLocation()), convert(e.getExpressionType()), parseIntegerLiteral(e.getRawSignature(), e));
    }
    
    assert e.getRawSignature().equals(String.valueOf(e.getValue()));

    return new IASTLiteralExpression(e.getRawSignature(), convert(e.getFileLocation()), convert(e.getExpressionType()), e.getKind());
  }

  private static char parseCharacterLiteral(String s, org.eclipse.cdt.core.dom.ast.IASTNode e) {
    check(s.charAt(0) == '\'' && s.charAt(s.length()-1) == '\'', "character literal without quotation marks", e);
    s = s.substring(1, s.length()-1); // remove the surrounding quotation marks ''
    check(s.length() > 0, "empty character literal", e);
    
    char result;
    if (s.length() == 1) {
      result = s.charAt(0);
    
    } else {
      check(s.charAt(0) == '\\', "character literal too long", e);
      // quoted character literal
      s = s.substring(1); // remove leading backslash \
      check(s.length() >= 1, "invalid quoting sequence", e);
      
      final char c = s.charAt(0);
      if (c == 'x') {
        // something like '\xFF'
        s = s.substring(1); // remove leading x
        check(s.length() > 0 && s.length() <= 3, "character literal with illegal hex number", e);
        try {
          result = (char) Integer.parseInt(s, 16);
          check(result <= 0xFF, "hex escape sequence out of range", e);
        } catch (NumberFormatException _) {
          throw new CFAGenerationRuntimeException("character literal with illegal hex number", e);
        }
      
      } else if (isDigit(c)) {
        // something like '\000'
        check(s.length() <= 3, "character literal with illegal octal number", e);
        try {
          result = (char)Integer.parseInt(s, 8);
          check(result <= 0xFF, "octal escape sequence out of range", e);
        } catch (NumberFormatException _) {
          throw new CFAGenerationRuntimeException("character literal with illegal octal number", e);
        }
        
      } else {
        // something like '\n'
        check(s.length() == 1, "character literal too long", e);
        switch (c) {
        case 'b'  : result = '\b'; break;
        case 't'  : result = '\t'; break;
        case 'n'  : result = '\n'; break;
        case 'f'  : result = '\f'; break;
        case 'r'  : result = '\r'; break;
        case '"'  : result = '\"'; break;
        case '\'' : result = '\''; break;
        case '\\' : result = '\\'; break;
        default   : throw new CFAGenerationRuntimeException("unknown character literal", e);
        }
      }
    }
    return result;
  }
  
  static BigInteger parseIntegerLiteral(String s, org.eclipse.cdt.core.dom.ast.IASTNode e) {
    // this might have some modifiers attached (e.g. 0ULL), we have to get rid of them
    int last = s.length()-1;
    int bits = 32;
    boolean signed = true;
        
    if (s.charAt(last) == 'L' || s.charAt(last) == 'l' ) {
      last--;
      // one 'L' is equal to no 'L' (TODO this assumes a 32bit machine)
    }
    if (s.charAt(last) == 'L' || s.charAt(last) == 'l') {
      last--;
      bits = 64; // two 'L' are a long long
    }
    if (s.charAt(last) == 'U' || s.charAt(last) == 'u') {
      last--;
      signed = false;
    }

    s = s.substring(0, last+1);
    BigInteger result;
    try {
      if (s.startsWith("0x") || s.startsWith("0X")) {
        // this should be in hex format, remove "0x" from the string
        s = s.substring(2);
        result = new BigInteger(s, 16);
      
      } else if (s.startsWith("0")) {
        result = new BigInteger(s, 8);
  
      } else {
        result = new BigInteger(s, 10);
      }
    } catch (NumberFormatException _) {
      throw new CFAGenerationRuntimeException("invalid number", e);
    }
    check(result.compareTo(BigInteger.ZERO) >= 0, "invalid number", e);
    
    // clear the bits that don't fit in the type 
    // a BigInteger with the lowest "bits" bits set to one (e. 2^32-1 or 2^64-1)
    BigInteger mask = BigInteger.ZERO.setBit(bits).subtract(BigInteger.ONE);
    result = result.and(mask);
    assert result.bitLength() <= bits;

    // compute twos complement if necessary
    if (signed && result.testBit(bits-1)) {
      // highest bit is set
      result = result.clearBit(bits-1);

      // a BigInteger for -2^(bits-1) (e.g. -2^-31 or -2^-63)
      BigInteger minValue = BigInteger.ZERO.setBit(bits-1).negate();
      
      result = minValue.add(result);
    }
    
    return result;
  }
  
  private static IASTExpression convert(org.eclipse.cdt.core.dom.ast.IASTUnaryExpression e) {
    if (e.getOperator() == org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_bracketedPrimary) {
      return convert(e.getOperand());
    }
    
    return new IASTUnaryExpression(e.getRawSignature(), convert(e.getFileLocation()), convert(e.getExpressionType()), convert(e.getOperand()), convertUnaryOperator(e));
  }
  
  private static UnaryOperator convertUnaryOperator(org.eclipse.cdt.core.dom.ast.IASTUnaryExpression e) {
    switch (e.getOperator()) {
    case org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_amper:
      return UnaryOperator.AMPER;
    case org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_minus:
      return UnaryOperator.MINUS;
    case org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_not:
      return UnaryOperator.NOT;
    case org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_plus:
      return UnaryOperator.PLUS;
    case org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_prefixIncr:
      return UnaryOperator.PREFIX_INCREMENT;
    case org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_prefixDecr:
      return UnaryOperator.PREFIX_DECREMENT;
    case org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_postFixIncr:
      return UnaryOperator.POSTFIX_INCREMENT;
    case org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_postFixDecr:
      return UnaryOperator.POSTFIX_DECREMENT;
    case org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_sizeof:
      return UnaryOperator.SIZEOF;
    case org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_star:
      return UnaryOperator.STAR;
    case org.eclipse.cdt.core.dom.ast.IASTUnaryExpression.op_tilde:
      return UnaryOperator.TILDE;
    default:
      throw new CFAGenerationRuntimeException("Unknown unary operator", e);
    }
  }

  private static IASTTypeIdExpression convert(org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression e) {
    return new IASTTypeIdExpression(e.getRawSignature(), convert(e.getFileLocation()), convert(e.getExpressionType()), e.getOperator(), convert(e.getTypeId()));
  }

  public static IASTStatement convert(final org.eclipse.cdt.core.dom.ast.IASTStatement s) {

    if (s instanceof org.eclipse.cdt.core.dom.ast.IASTExpressionStatement) {
      return convert((org.eclipse.cdt.core.dom.ast.IASTExpressionStatement) s);

    } else if (s instanceof org.eclipse.cdt.core.dom.ast.IASTReturnStatement) {
      return convert((org.eclipse.cdt.core.dom.ast.IASTReturnStatement) s);

    } else {
      throw new CFAGenerationRuntimeException("unknown statement: " + s.getClass(), s);
    }
  }

  public static IASTExpressionStatement convert(final org.eclipse.cdt.core.dom.ast.IASTExpressionStatement s) {
      return new IASTExpressionStatement(s.getRawSignature(), convert(s.getFileLocation()), convert(s.getExpression()));
  }
  
  public static IASTReturnStatement convert(final org.eclipse.cdt.core.dom.ast.IASTReturnStatement s) {
    return new IASTReturnStatement(s.getRawSignature(), convert(s.getFileLocation()), convert(s.getReturnValue()));
  }

  private static List<IASTDeclaration> convert(final org.eclipse.cdt.core.dom.ast.IASTDeclaration d) {
    if (d instanceof org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration) {
      return convert((org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration)d);
    
    } else {
      throw new CFAGenerationRuntimeException("unknown declaration type", d);
    }
  }
  
  public static IASTFunctionDefinition convert(final org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition f) {
    Pair<StorageClass, ? extends IType> specifier = convert(f.getDeclSpecifier());
    
    Triple<IType, IASTInitializer, IASTName> declarator = convert(f.getDeclarator(), specifier.getSecond());
    if (!(declarator.getFirst() instanceof IASTFunctionTypeSpecifier)) {
      throw new CFAGenerationRuntimeException("Unsupported nested declarator for function definition", f);
    }
    if (declarator.getSecond() != null) {
      throw new CFAGenerationRuntimeException("Unsupported initializer for function definition", f);
    }
    if (declarator.getThird() == null) {
      throw new CFAGenerationRuntimeException("Missing name for function definition", f);
    }
    
    IASTFunctionTypeSpecifier declSpec = (IASTFunctionTypeSpecifier)declarator.getFirst();
    
    // fake raw signature because otherwise it would contain the whole function body
    String rawSignature = f.getDeclSpecifier().getRawSignature() + " " + f.getDeclarator().getRawSignature();
    
    return new IASTFunctionDefinition(rawSignature, convert(f.getFileLocation()), specifier.getFirst(), declSpec, declarator.getThird());
  }
  
  public static List<IASTDeclaration> convert(final org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration d) {
    IASTFileLocation fileLoc = convert(d.getFileLocation());
    Pair<StorageClass, ? extends IType> specifier = convert(d.getDeclSpecifier());
    StorageClass storageClass = specifier.getFirst();
    IType type = specifier.getSecond();
    
    List<IASTDeclaration> result;
    org.eclipse.cdt.core.dom.ast.IASTDeclarator[] declarators = d.getDeclarators();
    if (declarators == null || declarators.length == 0) {
      // declaration without declarator, i.e. struct prototype
      IASTDeclaration newD = createDeclaration(d.getRawSignature(), fileLoc, storageClass, type, null);
      result = Collections.singletonList(newD);
    
    } else if (declarators.length == 1) {
      IASTDeclaration newD = createDeclaration(d.getRawSignature(), fileLoc, storageClass, type, declarators[0]);
      result = Collections.singletonList(newD);
    
    } else {
      result = new ArrayList<IASTDeclaration>(declarators.length);
      for (org.eclipse.cdt.core.dom.ast.IASTDeclarator c : d.getDeclarators()) {
        
        // fake rawSignature because otherwise the other declarators would appear in it, too
        String rawSignature = d.getDeclSpecifier().getRawSignature() + " " + c.getRawSignature() + ";";
        
        result.add(createDeclaration(rawSignature, fileLoc, storageClass, type, c));
      }
    }
    
    return result;
  }
  
  private static IASTDeclaration createDeclaration(String rawSignature, IASTFileLocation fileLoc, StorageClass storageClass, IType type, org.eclipse.cdt.core.dom.ast.IASTDeclarator d) {
    IASTInitializer initializer = null;
    IASTName name = null;
    
    if (d != null) {
      Triple<IType, IASTInitializer, IASTName> declarator = convert(d, type);
      type = declarator.getFirst();
      initializer = declarator.getSecond();
      name = declarator.getThird();
    }

    IASTSimpleDeclaration newSd = new IASTSimpleDeclaration(rawSignature, fileLoc, type, name);
    return new IASTDeclaration(rawSignature, fileLoc, storageClass, newSd, initializer);
  }
  
  private static Triple<IType, IASTInitializer, IASTName> convert(org.eclipse.cdt.core.dom.ast.IASTDeclarator d, IType specifier) {
    if (d instanceof org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator) {
      return convert((org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator)d, specifier);
      
    } else if (d instanceof org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator) {
      return convert((org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator)d, specifier);
    
    } else {
      if (d.getNestedDeclarator() != null) {
        throw new CFAGenerationRuntimeException("Nested declarator where not expected", d);
      }
      return Triple.of(
             convertPointerOperators(d.getPointerOperators(), specifier),
             convert(d.getInitializer()),
             convert(d.getName()));
    }
  }
  
  private static IType convertPointerOperators(org.eclipse.cdt.core.dom.ast.IASTPointerOperator[] ps, IType type) {
    for (org.eclipse.cdt.core.dom.ast.IASTPointerOperator p : ps) {
     
      if (p instanceof org.eclipse.cdt.core.dom.ast.IASTPointer) {
        type = convert((org.eclipse.cdt.core.dom.ast.IASTPointer)p, type);

      } else {
        throw new CFAGenerationRuntimeException("Unknown pointer operator", p);
      }
    }
    return type;
  }
  
  private static IASTPointerTypeSpecifier convert(org.eclipse.cdt.core.dom.ast.IASTPointer p, IType type) {
    return new IASTPointerTypeSpecifier(p.isConst(), p.isVolatile(), type);
  }
  
  private static Triple<IType, IASTInitializer, IASTName> convert(org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator d, IType type)  {
    IASTName name;
    if (d.getNestedDeclarator() != null) {
      Triple<? extends IType, IASTInitializer, IASTName> nestedDeclarator = convert(d.getNestedDeclarator(), type);
      
      assert d.getName().getRawSignature().isEmpty() : d;
      assert nestedDeclarator.getSecond() == null;
      
      type = nestedDeclarator.getFirst();
      name = nestedDeclarator.getThird();
    
    } else {
      name = convert(d.getName());
    }
    
    type = convertPointerOperators(d.getPointerOperators(), type);
    
    // TODO check order of pointer operators and array modifiers
    for (org.eclipse.cdt.core.dom.ast.IASTArrayModifier a : d.getArrayModifiers()) {

      if (a instanceof org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier) {
        type = convert((org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier)a, type);

      } else {
        throw new CFAGenerationRuntimeException("Unknown array modifier", a);
      }
    }
    return Triple.of(type, convert(d.getInitializer()), name);
  }
  
  private static IASTArrayTypeSpecifier convert(org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier a, IType type) {
    return new IASTArrayTypeSpecifier(a.isConst(), a.isVolatile(), type, convert(a.getConstantExpression()));
  }
  
  private static Triple<IType, IASTInitializer, IASTName> convert(org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator d, IType returnType) {
    if (!(d instanceof org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator)) {
      throw new CFAGenerationRuntimeException("Unknown non-standard function definition", d);
    }
    org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator sd = (org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator)d;

    // handle return type
    returnType = convertPointerOperators(d.getPointerOperators(), returnType);

    // handle parameters
    List<IASTSimpleDeclaration> paramsList = convert(sd.getParameters());
    
    // TODO constant and volatile
    IASTFunctionTypeSpecifier fType = new IASTFunctionTypeSpecifier(false, false, returnType, paramsList, sd.takesVarArgs());
    IType type = fType;
    
    IASTName name;
    if (d.getNestedDeclarator() != null) {
      Triple<? extends IType, IASTInitializer, IASTName> nestedDeclarator = convert(d.getNestedDeclarator(), type);
      
      assert d.getName().getRawSignature().isEmpty() : d;
      assert nestedDeclarator.getSecond() == null;

      type = nestedDeclarator.getFirst();
      name = nestedDeclarator.getThird();
    
    } else {
      name = convert(d.getName());
    }    
    
    fType.setName(name);
    
    return Triple.of(type, convert(d.getInitializer()), name);
  }
  
  
  private static Pair<StorageClass, ? extends IType> convert(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier d) {
    StorageClass sc = convertStorageClass(d);
    
    if (d instanceof org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier) {
      return Pair.of(sc, convert((org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier)d));
    
    } else if (d instanceof org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier) {
      return Pair.of(sc, convert((org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier)d));
      
    } else if (d instanceof org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier) {
      return Pair.of(sc, convert((org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier)d));
      
    } else if (d instanceof org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier) {
      return Pair.of(sc, convert((org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier)d));
      
    } else if (d instanceof org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier) {
      return Pair.of(sc, convert((org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier)d));
           
    } else {
      throw new CFAGenerationRuntimeException("unknown declSpecifier", d);
    }
  }
  
  private static StorageClass convertStorageClass(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier d) {
    switch (d.getStorageClass()) {
    case org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier.sc_unspecified:
    case org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier.sc_auto:
    case org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier.sc_register:
      return StorageClass.AUTO;
      
    case org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier.sc_static:
      return StorageClass.STATIC;

    case org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier.sc_extern:
      return StorageClass.EXTERN;

    case org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier.sc_typedef:
      return StorageClass.TYPEDEF;
      
    default:
      throw new CFAGenerationRuntimeException("Unsupported storage class", d);  
    }
  }
  
  private static IASTCompositeTypeSpecifier convert(org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier d) {
    List<IASTSimpleDeclaration> list = new ArrayList<IASTSimpleDeclaration>(d.getMembers().length);
    for (org.eclipse.cdt.core.dom.ast.IASTDeclaration c : d.getMembers()) {
      List<IASTDeclaration> newCs = convert(c);
      assert !newCs.isEmpty();

      for (IASTDeclaration newC : newCs) {
        if (newC.getStorageClass() != StorageClass.AUTO) {
          throw new CFAGenerationRuntimeException("Unsupported storage class inside composite type", c);
        }
        if (newC.getInitializer() != null) {
          throw new CFAGenerationRuntimeException("Unsupported initializer inside composite type", c);
        }
        list.add(newC.getDeclaration());
      }
    }
    return new IASTCompositeTypeSpecifier(d.isConst(), d.isVolatile(), d.getKey(), list, convert(d.getName()));
  }
  
  private static IASTElaboratedTypeSpecifier convert(org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier d) {
    return new IASTElaboratedTypeSpecifier(d.isConst(), d.isVolatile(), d.getKind(), convert(d.getName()));
  }
  
  private static IASTEnumerationSpecifier convert(org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier d) {
    List<IASTEnumerator> list = new ArrayList<IASTEnumerator>(d.getEnumerators().length);
    for (org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator c : d.getEnumerators()) {
      list.add(convert(c));
    }
    return new IASTEnumerationSpecifier(d.isConst(), d.isVolatile(), list, convert(d.getName()));
  }
  
  private static IASTNamedTypeSpecifier convert(org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier d) {
    return new IASTNamedTypeSpecifier(d.isConst(), d.isVolatile(), convert(d.getName()));
  }
  
  private static IASTSimpleDeclSpecifier convert(org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier d) {
    if (!(d instanceof org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier)) {
      throw new CFAGenerationRuntimeException("Unsupported type", d);
    }
    org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier dd = (org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier)d; 
    
    BasicType type;
    switch (dd.getType()) {
    case org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier.t_char:
      type = BasicType.CHAR;
      break;
    case org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier.t_double:
      type = BasicType.DOUBLE;
      break;
    case org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier.t_float:
      type = BasicType.FLOAT;
      break;
    case org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier.t_int:
      type = BasicType.INT;
      break;
    case org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier.t_unspecified:
      type = BasicType.UNSPECIFIED;
      break;
    case org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier.t_void:
      type = BasicType.VOID;
      break;
    default:
      throw new CFAGenerationRuntimeException("Unknown basic type", d); 
    }
    
    return new IASTSimpleDeclSpecifier(dd.isConst(), dd.isVolatile(), type,
        dd.isLong(), dd.isShort(), dd.isSigned(), d.isUnsigned(),
        dd.isComplex(), dd.isImaginary(), dd.isLongLong());
  }
  

  private static IASTEnumerator convert(org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator e) {
    return new IASTEnumerator(e.getRawSignature(), convert(e.getFileLocation()), convert(e.getName()), convert(e.getValue()));
  }

  private static IASTInitializer convert(org.eclipse.cdt.core.dom.ast.IASTInitializer i) {
    if (i == null) {
      return null;
    
    } else if (i instanceof org.eclipse.cdt.core.dom.ast.IASTInitializerExpression) {
      return convert((org.eclipse.cdt.core.dom.ast.IASTInitializerExpression)i);
    } else if (i instanceof org.eclipse.cdt.core.dom.ast.IASTInitializerList) {
      return convert((org.eclipse.cdt.core.dom.ast.IASTInitializerList)i);
    } else {
      throw new CFAGenerationRuntimeException("unknown initializer", i);
    }
  }
  
  private static IASTInitializerExpression convert(org.eclipse.cdt.core.dom.ast.IASTInitializerExpression i) {
    return new IASTInitializerExpression(i.getRawSignature(), convert(i.getFileLocation()), convert(i.getExpression()));
  }
  
  private static IASTInitializerList convert(org.eclipse.cdt.core.dom.ast.IASTInitializerList iList) {
    List<IASTInitializer> initializerList = new ArrayList<IASTInitializer>(iList.getInitializers().length);
    for (org.eclipse.cdt.core.dom.ast.IASTInitializer i : iList.getInitializers()) {
      initializerList.add(convert(i));
    }
    return new IASTInitializerList(iList.getRawSignature(), convert(iList.getFileLocation()), initializerList);
  }
  
  public static List<IASTSimpleDeclaration> convert(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration[] ps) {
    List<IASTSimpleDeclaration> paramsList = new ArrayList<IASTSimpleDeclaration>(ps.length);
    for (org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration c : ps) {
      if (!c.getRawSignature().equals("void")) {
        paramsList.add(convert(c));
      } else {
        // there may be a function declaration f(void), which is equal to f()
        // we don't want this dummy parameter "void"
        assert ps.length == 1;
      }
    }
    return paramsList;
  }
  
  public static IASTSimpleDeclaration convert(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration p) {
    Pair<StorageClass, ? extends IType> specifier = convert(p.getDeclSpecifier());
    if (specifier.getFirst() != StorageClass.AUTO) {
      throw new CFAGenerationRuntimeException("Unsupported storage class for parameters", p);
    }
    
    Triple<IType, IASTInitializer, IASTName> declarator = convert(p.getDeclarator(), specifier.getSecond());
    if (declarator.getSecond() != null) {
      throw new CFAGenerationRuntimeException("Unsupported initializer for parameters", p);
    }
    
    return new IASTSimpleDeclaration(p.getRawSignature(), convert(p.getFileLocation()), declarator.getFirst(), declarator.getThird());
  }
  
  private static IASTFileLocation convert(org.eclipse.cdt.core.dom.ast.IASTFileLocation l) {
    if (l == null) {
      return null;
    }
    return new IASTFileLocation(l.getEndingLineNumber(), l.getFileName(), l.getNodeLength(), l.getNodeOffset(), l.getStartingLineNumber());
  }
  
  private static IASTName convert(org.eclipse.cdt.core.dom.ast.IASTName n) {
    org.eclipse.cdt.core.dom.ast.IBinding binding = n.getBinding();
    if (binding == null) {
      binding = n.resolveBinding();
    }
    
    IType type;
    try {
      if (binding == null) {
        // not sure which C code triggers this 
        type = null;
        
      } else if (binding instanceof org.eclipse.cdt.core.dom.ast.IVariable) {
        type = convert(((org.eclipse.cdt.core.dom.ast.IVariable)binding).getType());
      
      } else if (binding instanceof org.eclipse.cdt.core.dom.ast.IFunction) {
        type = convert(((org.eclipse.cdt.core.dom.ast.IFunction)binding).getType());
      
      } else if (binding instanceof org.eclipse.cdt.core.dom.ast.IEnumerator) {
        type = convert(((org.eclipse.cdt.core.dom.ast.IEnumerator)binding).getType());
  
      } else {
        type = new DummyType(binding.getClass().getSimpleName());
      }
    } catch (org.eclipse.cdt.core.dom.ast.DOMException e) {
      throw new CFAGenerationRuntimeException(e.getMessage());
    }

    return new IASTName(n.getRawSignature(), convert(n.getFileLocation()), type);
  }
  
  private static IASTTypeId convert(org.eclipse.cdt.core.dom.ast.IASTTypeId t) {
    Pair<StorageClass, ? extends IType> specifier = convert(t.getDeclSpecifier());
    if (specifier.getFirst() != StorageClass.AUTO) {
      throw new CFAGenerationRuntimeException("Unsupported storage class for type ids", t);
    }
    
    Triple<IType, IASTInitializer, IASTName> declarator = convert(t.getAbstractDeclarator(), specifier.getSecond());
    if (declarator.getSecond() != null) {
      throw new CFAGenerationRuntimeException("Unsupported initializer for type ids", t);
    }
    
    return new IASTTypeId(t.getRawSignature(), convert(t.getFileLocation()), declarator.getFirst(), declarator.getThird());
  }
  
  private static IType convert(org.eclipse.cdt.core.dom.ast.IType t) {
    if (t instanceof org.eclipse.cdt.core.dom.ast.IBasicType) {
      return convert((org.eclipse.cdt.core.dom.ast.IBasicType)t);
 
    } else if (t instanceof org.eclipse.cdt.core.dom.ast.IPointerType) {
      return convert((org.eclipse.cdt.core.dom.ast.IPointerType)t);
      
    } else if (t instanceof org.eclipse.cdt.core.dom.ast.ITypedef) {
      return convert((org.eclipse.cdt.core.dom.ast.ITypedef)t);
      
    } else if (t instanceof org.eclipse.cdt.core.dom.ast.IBinding) {
      return new IComplexType(((org.eclipse.cdt.core.dom.ast.IBinding) t).getName());
      
    } else {
      return new DummyType(t.toString());
    }
  }

  private static IASTSimpleDeclSpecifier convert(final org.eclipse.cdt.core.dom.ast.IBasicType t) {

    try {
      
      BasicType type;
      switch (t.getType()) {
      case org.eclipse.cdt.core.dom.ast.IBasicType.t_char:
        type = BasicType.CHAR;
        break;
      case org.eclipse.cdt.core.dom.ast.IBasicType.t_double:
        type = BasicType.DOUBLE;
        break;
      case org.eclipse.cdt.core.dom.ast.IBasicType.t_float:
        type = BasicType.FLOAT;
        break;
      case org.eclipse.cdt.core.dom.ast.IBasicType.t_int:
        type = BasicType.INT;
        break;
      case org.eclipse.cdt.core.dom.ast.IBasicType.t_unspecified:
        type = BasicType.UNSPECIFIED;
        break;
      case org.eclipse.cdt.core.dom.ast.IBasicType.t_void:
        type = BasicType.VOID;
        break;
      default:
        throw new CFAGenerationRuntimeException("Unknown basic type " + t.getType()); 
      }
      
      // The IBasicType has to be an ICBasicType or
      // an IBasicType of type "void" (then it is an ICPPBasicType)
      if (t instanceof org.eclipse.cdt.core.dom.ast.c.ICBasicType) {
        final org.eclipse.cdt.core.dom.ast.c.ICBasicType c =
          (org.eclipse.cdt.core.dom.ast.c.ICBasicType) t;

        // TODO why is there no isConst() and isVolatile() here?
        return new IASTSimpleDeclSpecifier(false, false, type, c.isLong(), c.isShort(), c.isSigned(), c.isUnsigned(), c.isComplex(), c.isImaginary(), c.isLongLong());

      } else if (t.getType() == org.eclipse.cdt.core.dom.ast.IBasicType.t_void) {
          
        // the three values isComplex, isImaginary, isLongLong are initialized
        // with FALSE, because we do not know about them
        return new IASTSimpleDeclSpecifier(false, false, type, t.isLong(), t.isShort(), t.isSigned(), t.isUnsigned(), false, false, false);

      } else {
        throw new CFAGenerationRuntimeException("Unknown type " + t.toString());
      }
      
    } catch (org.eclipse.cdt.core.dom.ast.DOMException e) {
      throw new CFAGenerationRuntimeException(e.getMessage());
    }
  }
  
  private static IASTPointerTypeSpecifier convert(org.eclipse.cdt.core.dom.ast.IPointerType t) {
    try {
      return new IASTPointerTypeSpecifier(t.isConst(), t.isVolatile(), convert(t.getType()));
    } catch (org.eclipse.cdt.core.dom.ast.DOMException e) {
      throw new CFAGenerationRuntimeException(e.getMessage());
    }
  }
  
  private static ITypedef convert(org.eclipse.cdt.core.dom.ast.ITypedef t) {
    try {
      return new ITypedef(t.getName(), convert(t.getType()));
    } catch (org.eclipse.cdt.core.dom.ast.DOMException e) {
      throw new CFAGenerationRuntimeException(e.getMessage());
    }
  }
}
