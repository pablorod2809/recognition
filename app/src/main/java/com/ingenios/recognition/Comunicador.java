package com.ingenios.recognition;

/**
 * Created by pablorodriguez on 8/6/17.
 */
class Comunicador {
    private static Object objeto = null;

    public static void setObjeto(Object newObjeto) {
        objeto = newObjeto;
    }

    public static Object getObjeto() {
        return objeto;
    }
}