/**
 * Copyright (C) 2009-2012 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fusesource.restygwt.rebind;

import static org.fusesource.restygwt.rebind.BaseSourceCreator.DEBUG;
import static org.fusesource.restygwt.rebind.BaseSourceCreator.ERROR;
import static org.fusesource.restygwt.rebind.BaseSourceCreator.INFO;
import static org.fusesource.restygwt.rebind.BaseSourceCreator.TRACE;
import static org.fusesource.restygwt.rebind.BaseSourceCreator.WARN;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import org.fusesource.restygwt.client.AbstractJsonEncoderDecoder;
import org.fusesource.restygwt.client.Json;
import org.fusesource.restygwt.client.Json.Style;
import org.fusesource.restygwt.client.ObjectEncoderDecoder;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JArrayType;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.xml.client.Document;

/**
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class JsonEncoderDecoderInstanceLocator {

    public static final String JSON_ENCODER_DECODER_CLASS = AbstractJsonEncoderDecoder.class.getName();
    public static final String JSON_CLASS = Json.class.getName();

    public final JClassType STRING_TYPE;
    public final JClassType JSON_VALUE_TYPE;
    public final JClassType DOCUMENT_TYPE;
    public final JClassType MAP_TYPE;
    public final JClassType SET_TYPE;
    public final JClassType LIST_TYPE;

    public final HashMap<JType, String> builtInEncoderDecoders = new HashMap<JType, String>();

    public final GeneratorContext context;
    public final TreeLogger logger;

    public JsonEncoderDecoderInstanceLocator(GeneratorContext context, TreeLogger logger)
            throws UnableToCompleteException {
        this.context = context;
        this.logger = logger;

        this.STRING_TYPE = find(String.class);
        this.JSON_VALUE_TYPE = find(JSONValue.class);
        this.DOCUMENT_TYPE = find(Document.class);
        this.MAP_TYPE = find(Map.class);
        this.SET_TYPE = find(Set.class);
        this.LIST_TYPE = find(List.class);

        builtInEncoderDecoders.put(JPrimitiveType.BOOLEAN, JSON_ENCODER_DECODER_CLASS + ".BOOLEAN");
        builtInEncoderDecoders.put(JPrimitiveType.BYTE, JSON_ENCODER_DECODER_CLASS + ".BYTE");
        builtInEncoderDecoders.put(JPrimitiveType.CHAR, JSON_ENCODER_DECODER_CLASS + ".CHAR");
        builtInEncoderDecoders.put(JPrimitiveType.SHORT, JSON_ENCODER_DECODER_CLASS + ".SHORT");
        builtInEncoderDecoders.put(JPrimitiveType.INT, JSON_ENCODER_DECODER_CLASS + ".INT");
        builtInEncoderDecoders.put(JPrimitiveType.LONG, JSON_ENCODER_DECODER_CLASS + ".LONG");
        builtInEncoderDecoders.put(JPrimitiveType.FLOAT, JSON_ENCODER_DECODER_CLASS + ".FLOAT");
        builtInEncoderDecoders.put(JPrimitiveType.DOUBLE, JSON_ENCODER_DECODER_CLASS + ".DOUBLE");
        builtInEncoderDecoders.put(find(Boolean.class), JSON_ENCODER_DECODER_CLASS + ".BOOLEAN");
        builtInEncoderDecoders.put(find(Byte.class), JSON_ENCODER_DECODER_CLASS + ".BYTE");
        builtInEncoderDecoders.put(find(Character.class), JSON_ENCODER_DECODER_CLASS + ".CHAR");
        builtInEncoderDecoders.put(find(Short.class), JSON_ENCODER_DECODER_CLASS + ".SHORT");
        builtInEncoderDecoders.put(find(Integer.class), JSON_ENCODER_DECODER_CLASS + ".INT");
        builtInEncoderDecoders.put(find(Long.class), JSON_ENCODER_DECODER_CLASS + ".LONG");
        builtInEncoderDecoders.put(find(Float.class), JSON_ENCODER_DECODER_CLASS + ".FLOAT");
        builtInEncoderDecoders.put(find(Double.class), JSON_ENCODER_DECODER_CLASS + ".DOUBLE");
        builtInEncoderDecoders.put(find(BigDecimal.class), JSON_ENCODER_DECODER_CLASS + ".BIG_DECIMAL");
        builtInEncoderDecoders.put(find(BigInteger.class), JSON_ENCODER_DECODER_CLASS + ".BIG_INTEGER");

        builtInEncoderDecoders.put(STRING_TYPE, JSON_ENCODER_DECODER_CLASS + ".STRING");
        builtInEncoderDecoders.put(DOCUMENT_TYPE, JSON_ENCODER_DECODER_CLASS + ".DOCUMENT");
        builtInEncoderDecoders.put(JSON_VALUE_TYPE, JSON_ENCODER_DECODER_CLASS + ".JSON_VALUE");

        builtInEncoderDecoders.put(find(Date.class), JSON_ENCODER_DECODER_CLASS + ".DATE");
        
        builtInEncoderDecoders.put(find(Object.class), ObjectEncoderDecoder.class.getName() + ".INSTANCE");

    }

    private JClassType find(Class<?> type) throws UnableToCompleteException {
        return find(type.getName());
    }

    private JClassType find(String type) throws UnableToCompleteException {
        return RestServiceGenerator.find(logger, context, type);
    }

    private String getEncoderDecoder(JType type, TreeLogger logger) throws UnableToCompleteException {
        String rc = builtInEncoderDecoders.get(type);
        if (rc == null) {
            JClassType ct = type.isClass();
            if (ct != null && !isCollectionType(ct)) {
                JsonEncoderDecoderClassCreator generator = new JsonEncoderDecoderClassCreator(logger, context, ct);
                return generator.create() + ".INSTANCE";
            }
        }
        return rc;
    }

    public String encodeExpression(JType type, String expression, Style style) throws UnableToCompleteException {
        return encodeDecodeExpression(type, expression, style, "encode", JSON_ENCODER_DECODER_CLASS + ".toJSON", JSON_ENCODER_DECODER_CLASS + ".toJSON", JSON_ENCODER_DECODER_CLASS
                + ".toJSON", JSON_ENCODER_DECODER_CLASS + ".toJSON");
    }

    public String decodeExpression(JType type, String expression, Style style) throws UnableToCompleteException {
        return encodeDecodeExpression(type, expression, style, "decode", JSON_ENCODER_DECODER_CLASS + ".toMap", JSON_ENCODER_DECODER_CLASS + ".toSet", JSON_ENCODER_DECODER_CLASS
                + ".toList", JSON_ENCODER_DECODER_CLASS + ".toArray");
    }

    private String encodeDecodeExpression(JType type, String expression, Style style, String encoderMethod, String mapMethod, String setMethod, String listMethod, String arrayMethod)
            throws UnableToCompleteException {

        if (null != type.isEnum()) {
            if (encoderMethod.equals("encode")) {
                return encodeDecodeExpression(STRING_TYPE, expression + ".name()", style, encoderMethod, mapMethod, setMethod, listMethod, arrayMethod);
            } else {
                return type.getQualifiedSourceName() + ".valueOf(" + encodeDecodeExpression(STRING_TYPE, expression, style, encoderMethod, mapMethod, setMethod, listMethod, arrayMethod) + ")";
            }
        }

        String encoderDecoder = getEncoderDecoder(type, logger);
        if (encoderDecoder != null) {
            return encoderDecoder + "." + encoderMethod + "(" + expression + ")";
        }

        JClassType clazz = type.isClassOrInterface();

        if (isCollectionType(clazz)) {
            JParameterizedType parameterizedType = type.isParameterized();
            if (parameterizedType == null || parameterizedType.getTypeArgs() == null) {
                error("Collection types must be parameterized.");
            }
            JClassType[] types = parameterizedType.getTypeArgs();

            if (clazz.isAssignableTo(MAP_TYPE)) {
                if (types.length != 2) {
                    error("Map must define two and only two type parameters");
                }
                if (isCollectionType(types[0])) {
                    error("Map key can't be a collection");
                }
                if (!builtInEncoderDecoders.containsKey(types[0])) {
                    // Do not prevent a Map with an object key from compiling since some keys can be encoded/decoded
                    // error("Map key can't be an object");
                }
                String keyEncoderDecoder = getEncoderDecoder(types[0], logger);
                encoderDecoder = getEncoderDecoder(types[1], logger);

                // Encoder for Map with value of type List<String>
                // LIST_STRING is encoder/decoder for List<String>
                if (encoderDecoder == null && types[1].isAssignableTo(LIST_TYPE)) {
                    JParameterizedType listParameterizedType = types[1].isParameterized();
                    JClassType[] listTypes = listParameterizedType.getTypeArgs();
                    info("list encoding: " + types[1].toString());
                    if (listTypes.length == 1) {
                        if (listTypes[0].isAssignableTo(find(String.class))) {
                            encoderDecoder = JSON_ENCODER_DECODER_CLASS + ".LIST_STRING";
                            info("list assignable: " + encoderDecoder + Arrays.toString(listTypes));
                        }
                        else {
                            info("list not assignable (no encoder/decoder): " + encoderDecoder + Arrays.toString(listTypes));
                        }
                    }
                    else {
                        info("list not assignable (wrong size): " + types[1].toString());
                    }
                }

                if (encoderDecoder != null && keyEncoderDecoder != null) {
                    return mapMethod + "(" + expression + ", " + keyEncoderDecoder + ", " + encoderDecoder + ", "
                            + JSON_CLASS + ".Style." + style.name() + ")";
                } else if (encoderDecoder != null) {
                    return mapMethod + "(" + expression + ", " + encoderDecoder + ", " + JSON_CLASS + ".Style."
                            + style.name() + ")";
                }
            } else if (clazz.isAssignableTo(SET_TYPE)) {
                if (types.length != 1) {
                    error("Set must define one and only one type parameter");
                }
                encoderDecoder = getEncoderDecoder(types[0], logger);
                if (encoderDecoder != null) {
                    return setMethod + "(" + expression + ", " + encoderDecoder + ")";
                }
            } else if (clazz.isAssignableTo(LIST_TYPE)) {
                if (types.length != 1) {
                    error("List must define one and only one type parameter");
                }
                encoderDecoder = getEncoderDecoder(types[0], logger);
                info("type encoder for: " + types[0] + " is " + encoderDecoder);
                if (encoderDecoder != null) {
                    return listMethod + "(" + expression + ", " + encoderDecoder + ")";
                }
            }
        } else if (type.isArray() != null) {
            JType componentType = type.isArray().getComponentType();
            
            if (componentType.isArray() != null) {
                error("Multi-dimensional arrays are not yet supported");
            }
            
            encoderDecoder = getEncoderDecoder(componentType, logger);
            info("type encoder for: " + componentType + " is " + encoderDecoder);
            if (encoderDecoder != null) {
                if (encoderMethod.equals("encode")) {
                    return arrayMethod + "(" + expression + ", " + encoderDecoder + ")";
                } else {
                    return arrayMethod + "(" + expression + ", " + encoderDecoder
                            + ", new " + componentType.getQualifiedSourceName()
                            + "[" + JSON_ENCODER_DECODER_CLASS + ".getSize(" + expression + ")])";
                }
            }
        }

        error("Do not know how to encode/decode " + type);
        return null;
    }

    boolean isCollectionType(JClassType clazz) {
        return clazz != null
                && (clazz.isAssignableTo(SET_TYPE) || clazz.isAssignableTo(LIST_TYPE) || clazz.isAssignableTo(MAP_TYPE));
    }

    protected void error(String msg) throws UnableToCompleteException {
        logger.log(ERROR, msg);
        throw new UnableToCompleteException();
    }

    protected void warn(String msg) throws UnableToCompleteException {
        logger.log(WARN, msg);
        throw new UnableToCompleteException();
    }

    protected void info(String msg) throws UnableToCompleteException {
        logger.log(INFO, msg);
    }

    protected void debug(String msg) throws UnableToCompleteException {
        logger.log(DEBUG, msg);
    }

    protected void trace(String msg) throws UnableToCompleteException {
        logger.log(TRACE, msg);
    }

}
