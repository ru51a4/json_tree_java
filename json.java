import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

class node {
    Map<String, node> children;
    List<node> childrenArray = new ArrayList<node>();
    Map<String, String> values;
    List<String> valuesPrimitive = new ArrayList<String>();
    nodeType nodeType;
}

class token {
    public token(String _value, tokenType _tokenType) {
        this.value = _value;
        this.tokenType = _tokenType;
    }

    public String value;
    public tokenType tokenType;
}

interface Next {
    public char run();
}

interface NextNext {
    public char run(char val);
}

enum tokenType {
    openObj,
    open,
    openArray,
    primitive,
    value,
    closed,
}

enum nodeType {
    object,
    array
}

class lexer {
    static public List<token> lex(String str) {
        List<token> tokens = new ArrayList<token>();
        String t = "";
        AtomicReference<Integer> ZaebJ = new AtomicReference<>(0);
        AtomicReference<Boolean> isString = new AtomicReference<>(false);
        Next next = () -> {
            Integer j = ZaebJ.get();
            for (int i = j; i <= str.length() - 1; i++) {
                if (str.charAt(i) == ' ' && isString.get()) {
                    j = i + 1;
                    ZaebJ.set(j);
                    return str.charAt(i);
                }
                if (str.charAt(i) == ' ' || str.charAt(i) == '\n' || str.charAt(i) == '\t' || str.charAt(i) == '\r') {
                    if (i == str.length() - 1) {
                        j = i + 1;
                    }
                    continue;
                } else {
                    j = i + 1;
                    ZaebJ.set(j);
                    return str.charAt(i);
                }
            }
            ZaebJ.set(j);
            return 'x';
        };

        NextNext nextnext = (val) -> {
            Integer j = ZaebJ.get();
            for (int i = j; i <= str.length() - 1; i++) {
                if (str.charAt(i) == ' ' || str.charAt(i) == '"' || str.charAt(i) == '\n'
                        || str.charAt(i) == '\t' || str.charAt(i) == '\r') {
                    continue;
                } else {
                    if (str.charAt(i) == val) {
                        j = i + 1;
                        ZaebJ.set(j);
                        return str.charAt(i);
                    } else {
                        ZaebJ.set(j);
                        return 'x';
                    }
                }
            }
            ZaebJ.set(j);
            return 'x';
        };

        for (int i = 0; i <= str.length() - 1; i++) {
            char cChar = next.run();
            if (cChar == '\"') {
                isString.set(!isString.get());
                continue;
            }
            if (cChar == '{') {
                token _c = new token(t, tokenType.openObj);
                tokens.add(_c);
                t = "";
            } else if (cChar == ':' && nextnext.run('{') == '{') {
                token _c = new token(t, tokenType.open);
                tokens.add(_c);
                t = "";
            } else if (cChar == ':' && nextnext.run('[') == '[') {
                token _c = new token(t, tokenType.openArray);
                tokens.add(_c);
                t = "";
            } else if (cChar == ',' || cChar == '}' || cChar == ']') {
                if (str.length() > 0) {
                    Boolean isPrimitive = t.contains(":");
                    if (!isPrimitive) {
                        token _c = new token(t, tokenType.primitive);
                        tokens.add(_c);
                    } else {
                        token _c = new token(t, tokenType.value);
                        tokens.add(_c);
                    }
                    t = "";
                }
                if (cChar == '}' || cChar == ']') {
                    token _c = new token(t, tokenType.closed);
                    tokens.add(_c);
                }
            } else {
                t = t + cChar;
            }
        }
        return tokens;

    }

}

class Json {
    public static void main(String[] args) throws IOException {
        Path fileName = Path.of("./input.json");
        String b = Files.readString(fileName);
        Stack<node> stack = new Stack<>();
        List<token> tokens = lexer.lex(b);
        tokenType initToken = tokens.get(0).tokenType;
        tokens.remove(0);
        if (initToken == tokenType.openObj) {
            node _c = new node();
            _c.children = new HashMap<String, node>();
            _c.nodeType = nodeType.object;
            stack.push(_c);
        } else {
            // todo
        }
        while (tokens.size() > 1) {
            token cToken = tokens.get(0);
            tokens.remove(0);
            if (cToken.tokenType == tokenType.open || cToken.tokenType == tokenType.openObj
                    || cToken.tokenType == tokenType.openArray) {
                node _c = new node();
                _c.children = new HashMap<String, node>();
                _c.values = new HashMap<String, String>();
                node tObj = stack.lastElement();
                stack.push(_c);
                if (cToken.tokenType == tokenType.openArray) {
                    _c.nodeType = nodeType.array;
                } else {
                    _c.nodeType = nodeType.object;
                }
                if (cToken.tokenType == tokenType.openObj) {
                    tObj.childrenArray.add(_c);
                } else {
                    tObj.children.put(cToken.value, _c);
                }
            } else if (cToken.tokenType == tokenType.value) {
                String[] result = cToken.value.split(":");
                node tObj = stack.lastElement();
                tObj.values.put(result[0], result[1]);
            } else if (cToken.tokenType == tokenType.primitive) {
                node tObj = stack.lastElement();
                tObj.valuesPrimitive.add(cToken.value);
            } else if (cToken.tokenType == tokenType.closed) {
                stack.pop();
            }
        }
        System.out.println(stack.get(0).children.get("menu").values.get("header"));
    }
}