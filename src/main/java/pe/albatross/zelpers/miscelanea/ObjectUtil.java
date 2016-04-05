package pe.albatross.zelpers.miscelanea;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectUtil {

    private static final Logger logger = LoggerFactory.getLogger(ObjectUtil.class);

    public static Object completarAtributoObjeto(Object obj, String atributo) {
        Object objAttr = null;
        Method metodo = null;
        objAttr = getParent(obj, atributo);
        metodo = getMethod(obj, atributo);

        if (objAttr != null) {
            return objAttr;
        }

        try {
            String[] tipoReturnList = metodo.getReturnType().toString().split(" ");
            Class clasePadre = Class.forName(tipoReturnList[tipoReturnList.length - 1]);
            Constructor constructor = clasePadre.getConstructor();
            objAttr = constructor.newInstance();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }

        StringBuilder sbMetodo = new StringBuilder("set");
        sbMetodo.append(WordUtils.capitalize(atributo));
        Method[] metodos = obj.getClass().getMethods();

        for (Method metodoTmp : metodos) {
            if (sbMetodo.toString().equals(metodoTmp.getName())) {
                metodo = metodoTmp;
                break;
            }
        }

        try {
            metodo.invoke(obj, objAttr);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }

        return objAttr;
    }

    public static void completarParents(Object obj, String parents) {
        Object padre = obj;
        String[] padreLista = parents.split("\\.");
        for (int i = 0; i < padreLista.length; i++) {
            String parent = padreLista[i];
            padre = completarAtributoObjeto(padre, parent);
        }

    }

    public static Object getParent(Object obj, String atributo) {
        Object parent = null;
        Method metodo = null;
        StringBuilder sbMetodo = new StringBuilder("get");
        sbMetodo.append(WordUtils.capitalize(atributo));

        for (Method metodoTmp : obj.getClass().getMethods()) {
            if (sbMetodo.toString().equals(metodoTmp.getName())) {
                metodo = metodoTmp;
                break;
            }
        }
        try {
            parent = metodo.invoke(obj);
        } catch (InvocationTargetException ex) {
        } catch (Exception ex) {
            logger.error("InvocationTargetException ::: " + ex.getLocalizedMessage());
        }

        return parent;
    }

    public static Object getParentTree(Object obj, String atributo) {
        Object objPadre = null;
        Object objHijo = obj;
        String[] attrs = atributo.split("\\.");

        for (String attr : attrs) {
            objPadre = getParent(objHijo, attr);
            if (objPadre == null) {
                return null;
            }
            objHijo = objPadre;
        }

        return objPadre;
    }

    public static Method getMethod(Object obj, String atributo) {
        Method metodo = null;
        StringBuilder sbMetodo = new StringBuilder("get");
        sbMetodo.append(WordUtils.capitalize(atributo));

        for (Method metodoTmp : obj.getClass().getMethods()) {
            if (sbMetodo.toString().equals(metodoTmp.getName())) {
                metodo = metodoTmp;
                break;
            }
        }

        return metodo;
    }

    public static void eliminarAttrSinId(Object obj, String atributo) {
        Object objNieto = null;
        Object objPadre = null;
        Object objHijo = obj;

        String[] attrs = atributo.split("\\.");

        for (String attr : attrs) {
            objPadre = getParent(objHijo, attr);
            objNieto = objHijo;
            objHijo = objPadre;
            if (objPadre == null) {
                break;
            }
        }

        if (objPadre == null) {
            return;
        }

        Method metodo = null;
        for (Method metodoTmp : objHijo.getClass().getMethods()) {
            if ("getId".equals(metodoTmp.getName())) {
                metodo = metodoTmp;
                break;
            }
        }

        Object objId = null;

        try {
            objId = metodo.invoke(objHijo);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }

        if (objId == null) {
            String attr = attrs[attrs.length - 1];
            StringBuilder sbMetodo = new StringBuilder("set");
            sbMetodo.append(WordUtils.capitalize(attr));
            for (Method metodoTmp : objNieto.getClass().getMethods()) {
                if (sbMetodo.toString().equals(metodoTmp.getName())) {
                    metodo = metodoTmp;
                    break;
                }
            }

            try {
                metodo.invoke(objNieto, new Object[]{null});
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }

        }

    }

    public static void printAttr(Object obj) {
        Method[] methods = obj.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get") && method.getGenericParameterTypes().length == 0) {
                try {
                    Object returnObject = method.invoke(obj);
                    logger.debug(method.getName() + " - " + returnObject);
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                }
            }

        }
    }

    public static boolean verificarIgualdad(Object obj1, Object obj2, List<String> atributos) {
        if (atributos.size() == 0) {
            throw new PhobosException("Lista de parametros vacio");
        }

        int i = 0;
        for (String atributo : atributos) {
            try {
                i += compararAtributos(obtenerCampo(obj1, atributo), obtenerCampo(obj2, atributo));
            } catch (Exception ex) {
                logger.debug(ex.getLocalizedMessage());
            }
        }

        if (i > 0) {
            return false;
        } else {
            return true;
        }
    }

    public static Integer compararAtributos(Object attr1, Object attr2) {
        if (attr1 instanceof Date && attr2 instanceof Date) {
            Date fecha1 = (Date) attr1;
            Date fecha2 = (Date) attr2;
            if (fecha1.equals(fecha2)) {
                return 0;
            } else {
                return 1;
            }
        }
        if (attr1 == null && attr2 == null) {
            return 0;
        }
        if (attr1 == null && attr2 != null) {
            return 1;
        }
        if (attr1 != null && attr2 == null) {
            return 1;
        }
        return Math.abs(attr1.toString().compareTo(attr2.toString()));
    }

    public static boolean equalsAttrs(Object obj1, Object obj2, List<String> atributos) {
        if (atributos.size() == 0) {
            throw new PhobosException("Lista de parametros vacio");
        }

        int i = 0;
        for (String atributo : atributos) {
            try {
                i += equalAttr(getParentTree(obj1, atributo), getParentTree(obj2, atributo));
            } catch (Exception ex) {
                logger.debug(ex.getLocalizedMessage());
            }
        }

        if (i > 0) {
            return false;
        } else {
            return true;
        }
    }

    public static Integer equalAttr(Object attr1, Object attr2) {

        if (attr1 == null && attr2 == null) {
            return 0;
        }
        if (attr1 == null && attr2 != null) {
            return 1;
        }
        if (attr1 != null && attr2 == null) {
            return 1;
        }

        if (attr1 instanceof Date && attr2 instanceof Date) {
            Date fecha1 = (Date) attr1;
            Date fecha2 = (Date) attr2;
            if (fecha1.equals(fecha2)) {
                return 0;
            } else {
                return 1;
            }
        }
        return Math.abs(attr1.toString().compareTo(attr2.toString()));
    }

    public static Object obtenerCampo(Object obj, String nombre) throws Exception {
        Field f = obj.getClass().getDeclaredField(nombre);
        f.setAccessible(true);
        Object iWantThis = f.get(obj);
        return iWantThis;
    }
}
