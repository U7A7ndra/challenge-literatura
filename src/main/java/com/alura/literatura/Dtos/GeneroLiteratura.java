package com.alura.literatura.Dtos;

public enum
GeneroLiteratura {
    ACCION ("Action"),
    ROMANCE ("Romance"),
    CRIMEN ("Crime"),
    COMEDIA ("Comedy"),
    DRAMA ("Drama"),
    AVENTURA ("Adventure"),
    FICCION ("Fiction"),
    DESCONOCIDO("Desconocido");
    private String genero;

    GeneroLiteratura(String generodeGutendex) {
        this.genero = generodeGutendex;
    }

    public static GeneroLiteratura fromString(String text){
        for (GeneroLiteratura generoEnum: GeneroLiteratura.values()){
            if (generoEnum.genero.equals(text)){
                return generoEnum;
            }
        }
        return GeneroLiteratura.DESCONOCIDO;
    }
}
