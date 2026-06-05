package com.example.domus.domain.repository;

import com.example.domus.domain.model.Morador;
import org.json.JSONObject;

public interface MoradorRepository {
    JSONObject validateLogin(String usuario, String senha);
    Morador getMoradorByUsuario(String usuario);
    boolean saveMorador(Morador morador);
}