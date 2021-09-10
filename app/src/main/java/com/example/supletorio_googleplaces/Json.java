package com.example.supletorio_googleplaces;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Json {
    private HashMap<String,String> parseJsonObject(JSONObject jsonObject){
        HashMap<String,String> hashMap= new HashMap<>();

        try {
            String name= jsonObject.getString("name");
            String longitud = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lng");
            String latitud = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat");

            hashMap.put("name",name);
            hashMap.put("lat", latitud);
            hashMap.put("lng",longitud);

        }catch (JSONException e){
            e.printStackTrace();
        }

        return hashMap;

    }
    public List<HashMap<String,String>> resultParse(JSONObject object){
        JSONArray jsonArray= null;
        try {
            jsonArray=object.getJSONArray("results");

        }catch (JSONException e){
            e.printStackTrace();
        }
        return arrayJsonPase(jsonArray);
    }


    private List<HashMap<String,String>> arrayJsonPase(JSONArray jsonArray){

        List<HashMap<String, String>> dataList= new ArrayList<>();
        for (int i=0; i<jsonArray.length();i++){
            try {
                HashMap<String,String> data= parseJsonObject((JSONObject) jsonArray.get(i));
                dataList.add(data);


            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return  dataList;
    }




}
