package org.analizador.partesintactica;

import java.util.HashMap;

public class TablaSimbolos {

    private HashMap<String, Integer> table = new HashMap<>();

    public void put(String id, int value){
        table.put(id, value);
    }

    public int get(String id){
        return table.getOrDefault(id, 0);
    }

    public boolean contains(String id){
        return table.containsKey(id);
    }
}
