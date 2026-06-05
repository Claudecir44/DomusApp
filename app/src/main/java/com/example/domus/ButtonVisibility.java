package com.example.domus.domain.model;

import java.util.HashMap;
import java.util.Map;

public class ButtonVisibility {
    private final Map<String, Boolean> visibilityMap;

    private ButtonVisibility(Map<String, Boolean> visibilityMap) {
        this.visibilityMap = visibilityMap;
    }

    public static ButtonVisibility forAdmin() {
        Map<String, Boolean> map = new HashMap<>();
        String[] buttons = {
                "cadastro", "lista", "backup", "ocorrencias", "funcionarios",
                "manutencao", "assembleias", "despesas", "administradores", "avisos",
                "listaAssembleias", "listaDespesas", "listaAvisos"
        };
        for (String button : buttons) {
            map.put(button, true);
        }
        return new ButtonVisibility(map);
    }

    public static ButtonVisibility forMorador() {
        Map<String, Boolean> map = new HashMap<>();
        // Todos false por padrão
        String[] allButtons = {
                "cadastro", "lista", "backup", "ocorrencias", "funcionarios",
                "manutencao", "assembleias", "despesas", "administradores", "avisos"
        };
        for (String button : allButtons) {
            map.put(button, false);
        }
        // Apenas listagens visíveis
        map.put("listaAssembleias", true);
        map.put("listaDespesas", true);
        map.put("listaAvisos", true);
        return new ButtonVisibility(map);
    }

    public boolean isVisible(String buttonKey) {
        return visibilityMap.getOrDefault(buttonKey, false);
    }

    public Map<String, Boolean> getVisibilityMap() {
        return new HashMap<>(visibilityMap);
    }
}