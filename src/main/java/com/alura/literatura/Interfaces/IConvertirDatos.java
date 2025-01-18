package com.alura.literatura.Interfaces;

public interface IConvertirDatos {

    <T> T convertirDatosJsonAJava(String json , Class<T> clase);

}