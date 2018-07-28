package com.example.radu5.turistgroupchat.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.radu5.turistgroupchat.Liste.MyListeListFragment;
import com.example.radu5.turistgroupchat.Model.BusRoute;
import com.example.radu5.turistgroupchat.Model.BusStop;
import com.example.radu5.turistgroupchat.Model.BusTrip;
import com.example.radu5.turistgroupchat.Model.ChatGroup;
import com.example.radu5.turistgroupchat.Model.ListItem;
import com.example.radu5.turistgroupchat.UserApp.UserLoginActivity;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by radu5 on 3/23/2018.
 */

public class ApiHelper {
    private static final String TAG = "ApiHelper";
    private static final String flikrApiKey="24ce0f1149375f87266918264dcd4376";

    //todo GET DATA
    public static byte[] getUrlBytes(RequestPackage requestPackage) throws IOException{
            String adress=requestPackage.getEndpoint();
            String encodedParams=requestPackage.getEncodedParams();
            if(requestPackage.getMethod().equals("GET")&&
                    encodedParams.length()>0){
                adress=String.format("%s?%s",adress,encodedParams);
            }
            URL url=new URL(adress);
            final int READ_TIMEOUT = 30000;
            final int CONNECTION_TIMEOUT = 30000;
            HttpURLConnection connection=(HttpURLConnection)url.openConnection();
            if(requestPackage.getToken()!=null){
                //add token
                String basicAuth = "Bearer " +requestPackage.getToken();
                connection.setRequestProperty("Authorization",basicAuth);
                Log.d(TAG, "getUrlBytes: using token:"+basicAuth);
            }
            connection.setRequestMethod(requestPackage.getMethod());
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setDoInput(true);

            if(requestPackage.getMethod().equals("POST")&& encodedParams.length()>0){
                connection.setDoOutput(true);
                OutputStreamWriter writer=new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                writer.write(requestPackage.getEncodedParams());
                writer.flush();
                writer.close();
            }
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {//200
                    throw new IOException(connection.getResponseMessage() + ": with " + adress);
                }
                InputStream in = connection.getInputStream();
                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while ((bytesRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                return out.toByteArray();
            }
            finally{
                connection.disconnect();
            }
    }

    //todo Convert get bites into string
    public static String getUrlString(RequestPackage requestPackage) throws IOException {
        return new String(getUrlBytes(requestPackage));
    }

    //todo post data
    public static int busLogin(String name,String pass){
        int id=-1;
        RequestPackage requestPackage=new RequestPackage();
        requestPackage.setEndPoint("http://raduhdd.asuscomm.com:3000/api/bus-tracking/login");
        requestPackage.setMethod("POST");
        requestPackage.setParam("registration_number",name);
        requestPackage.setParam("password",pass);

        try{
           String jsonString=getUrlString(requestPackage);
            //String jsonString=getUrlStringOKHTTP(requestPackage);
            JSONObject jsonBody=new JSONObject(jsonString);
            Log.i(TAG, "Received JSON: " + jsonBody.toString());

            if(jsonBody.getString("message").equals("Succes Login")){
                id=jsonBody.getInt("bus_route_id");
            }

            //daca ajunge aici => code 200 , daca login failed e code 401 si se duce in catch
            Log.d(TAG, "busLogin: succes");

        }catch(Exception e){
            System.out.println(e.getMessage());e.printStackTrace();
            Log.d(TAG, "busLogin: failed");
        }
        Log.d(TAG, "busLogin: id:"+id);
        return id;//-1 = failed
    }

    //todo transform string into array of data object
    public static BusRoute getBusRounte(int busRouteId){
       // Gson gson=new Gson();
        BusRoute busRoute=null;

        RequestPackage requestPackage=new RequestPackage();
        requestPackage.setEndPoint("http://raduhdd.asuscomm.com:3000/api/bus-tracking/routes/"+busRouteId);
        //requestPackage.setParam("id",String.valueOf(busRouteId));
        requestPackage.setMethod("GET");
        requestPackage.setToken("Test");

        try{

            String jsonString=getUrlString(requestPackage);
           // String jsonString=getUrlStringOKHTTP(requestPackage);//cu OKHTTP
            busRoute=new BusRoute();
            JSONObject jsonBody=new JSONObject(jsonString);
            parseBusRoute(busRoute,jsonBody);
            Log.d(TAG, "getBusRounte: "+jsonBody.toString());


        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return busRoute;
    }
    //todo datele vin ordonate calumea de la server
    private static void parseBusRoute(BusRoute busRoute,JSONObject jsonBody)throws IOException, JSONException{
        JSONArray tripsJsonArray=jsonBody.getJSONArray("trips");

        List<BusTrip> trips = new ArrayList<>();//Trips din ruta

        for(int i = 0 ; i <tripsJsonArray.length();i++){
            JSONObject tripJsonObject=tripsJsonArray.getJSONObject(i);
            BusTrip busTrip=new BusTrip();
            List<BusStop> stops=new ArrayList<>();
            JSONArray statiiJsonArray=tripJsonObject.getJSONArray("statii");
            for(int j = 0 ; j < statiiJsonArray.length();j++){
                JSONObject statieJsonObject=statiiJsonArray.getJSONObject(j);
                BusStop busStop=new BusStop();
                busStop.setStopId(statieJsonObject.getInt("stop_id"));
                busStop.setStopName(statieJsonObject.getString("stop_name"));
                busStop.setLat(statieJsonObject.getDouble("lat"));
                busStop.setLng(statieJsonObject.getDouble("lng"));
                busStop.setOrder(statieJsonObject.getInt("order"));
                stops.add(busStop);
            }
            busTrip.setTripId(tripJsonObject.getInt("trip_id"));
            busTrip.setTripName(tripJsonObject.getString("trip_name"));
            busTrip.setDirection(tripJsonObject.getInt("direction"));
            busTrip.setStatii(stops);
            trips.add(busTrip);
        }
        busRoute.setBusRouteName(jsonBody.getString("bus_route_name"));
        busRoute.setBusRouteId(jsonBody.getInt("bus_route_id"));
        busRoute.setTrips(trips);
    }
    //todo get all routes in a city
    public static ArrayList<BusRoute> getBusRoutesCity(int city_id){
        // Gson gson=new Gson();
        ArrayList<BusRoute> a=null;

        RequestPackage requestPackage=new RequestPackage();
        requestPackage.setEndPoint("http://raduhdd.asuscomm.com:3000/api/bus-tracking/routes/city/"+city_id);
        //requestPackage.setParam("id",String.valueOf(busRouteId));
        requestPackage.setMethod("GET");
        requestPackage.setToken("Test");
        try{
            String jsonString=getUrlString(requestPackage);
            a=new ArrayList<>();
            JSONObject jsonBody=new JSONObject(jsonString);
            parseBusRoutes(a,jsonBody);
            Log.d(TAG, "getBusRoutesCity: "+jsonBody.toString());


        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return a;
    }
    private static void parseBusRoutes(ArrayList<BusRoute> x,JSONObject jsonBody)throws IOException, JSONException{

        JSONArray busRoutesJsonArray=jsonBody.getJSONArray("routes");

        for(int i = 0 ; i < busRoutesJsonArray.length();i++){
            BusRoute b=new BusRoute();
            parseBusRoute(b,busRoutesJsonArray.getJSONObject(i));
            x.add(b);
        }
    }
    //todo get liste
    public static ArrayList<com.example.radu5.turistgroupchat.Model.List> getListeTuristice(int pagina, int city_id, MyListeListFragment a){
        ArrayList<com.example.radu5.turistgroupchat.Model.List> liste=new ArrayList<>();
        RequestPackage requestPackage=new RequestPackage();
        requestPackage.setEndPoint("http://raduhdd.asuscomm.com:3000/api/lists");
        requestPackage.setMethod("GET");
        requestPackage.setParam("city_id",String.valueOf(city_id));
        requestPackage.setParam("page",String.valueOf(pagina));
        requestPackage.setParam("limit","10");

        try{
            String jsonString=getUrlString(requestPackage);
            JSONObject jsonObject=new JSONObject(jsonString);
            parseListeItems(liste,jsonObject);
            int maxPage=jsonObject.getInt("maxPage");
            a.setMaxPage(maxPage);
        } catch (IOException e) {
            e.printStackTrace();
        }catch(JSONException joe) {
            Log.e(TAG, "Failed to parse JSON", joe);
        }
        return liste;
    }
    private static void parseListeItems(ArrayList<com.example.radu5.turistgroupchat.Model.List> items,JSONObject jsonBody)throws IOException, JSONException{
        JSONArray listeJsonArray=jsonBody.getJSONArray("liste");
        for(int i = 0 ; i <listeJsonArray.length();i++){
            JSONObject listaJsonObject=listeJsonArray.getJSONObject(i);
            com.example.radu5.turistgroupchat.Model.List item=new com.example.radu5.turistgroupchat.Model.List();
            item.setId(listaJsonObject.getInt("id"));
            item.setCreator(listaJsonObject.getInt("creator"));
            item.setTitle(listaJsonObject.getString("title"));
            item.setDescription(listaJsonObject.getString("description"));
            item.setCity_id(listaJsonObject.getInt("city_id"));
            item.setThumbnailUrl(listaJsonObject.getString("thumbnail"));
            items.add(item);
        }
    }
    //todo get all items in list
    public static ArrayList<ListItem> getListItems(int listId){
        ArrayList<ListItem> listItems=new ArrayList<>();
        RequestPackage requestPackage=new RequestPackage();
        requestPackage.setEndPoint("http://raduhdd.asuscomm.com:3000/api/lists/"+listId);
        requestPackage.setMethod("GET");
        try{
            String jsonString=getUrlString(requestPackage);
            JSONObject jsonObject=new JSONObject(jsonString);
            parseItemsList(listItems,jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  listItems;
    }
    private static void parseItemsList(ArrayList<ListItem> items,JSONObject jsonObject)throws IOException, JSONException{
        JSONArray i=jsonObject.getJSONArray("listItems");
        for(int j=0;j<i.length();j++){
            JSONObject k=i.getJSONObject(j);
            ListItem o=new ListItem(k.getInt("id"),k.getString("name"),k.getString("description"),k.getDouble("lat"),k.getDouble("lng"),k.getString("thumbnail"));
            items.add(o);
        }

    }

    public static int userLogin(String name,String pass,Context ctx){
        int id=-1;
        String authToken="";
        RequestPackage requestPackage=new RequestPackage();
        requestPackage.setEndPoint("http://raduhdd.asuscomm.com:3000/api/users/login");
        requestPackage.setMethod("POST");
        requestPackage.setParam("name",name);
        requestPackage.setParam("pass",pass);

        try{
            String jsonString=getUrlString(requestPackage);
            JSONObject jsonBody=new JSONObject(jsonString);
            Log.i(TAG, "Received JSON: " + jsonBody.toString());

            if(jsonBody.getString("message").equals("Succes Login")){
                id=jsonBody.getInt("id");
                authToken=jsonBody.getString("token");
                Log.d(TAG, "token:"+authToken);
                //pastrare token
                SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(ctx);
                //pref.getstring pref.getString("username", "n/a");
                Log.d(TAG, "Old Token: "+pref.getString("token","n/a"));
                SharedPreferences.Editor edit=pref.edit();
                edit.putString("token",authToken);
                edit.apply();
            }
            //daca ajunge aici => code 200 , daca login failed e code 401 si se duce in catch
            Log.d(TAG, "Login: succes");

        }catch(Exception e){
            System.out.println(e.getMessage());e.printStackTrace();
            Log.d(TAG, "Login: failed");
        }
        Log.d(TAG, "Login: id:"+id);
        return id;//-1 = failed
    }


    public static int userRegister(String name,String pass,Context ctx){
        int id=-1;
        String authToken="";
        RequestPackage requestPackage=new RequestPackage();
        requestPackage.setEndPoint("http://raduhdd.asuscomm.com:3000/api/users/register");
        requestPackage.setMethod("POST");
        requestPackage.setParam("name",name);
        requestPackage.setParam("pass",pass);

        try{
            String jsonString=getUrlString(requestPackage);
            JSONObject jsonBody=new JSONObject(jsonString);
            Log.i(TAG, "Received JSON: " + jsonBody.toString());

            if(jsonBody.getString("message").equals("Succes Register")){
                id=jsonBody.getInt("id");
                authToken=jsonBody.getString("token");
                //pastrare token
                SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(ctx);
                //pref.getstring pref.getString("username", "n/a");
                Log.d(TAG, "userRegister:Old Token "+pref.getString("token","n/a"));
                SharedPreferences.Editor edit=pref.edit();
                edit.putString("token",authToken);
                edit.apply();

            }
            else{
                Activity a=(Activity) ctx;
                a.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ctx,"Register Failed:"+jsonBody.toString(),Toast.LENGTH_LONG).show();
                    }
                });

            }
            //daca ajunge aici => code 200 , daca login failed e code 401 si se duce in catch

        }catch(Exception e){
            System.out.println(e.getMessage());e.printStackTrace();
            Log.d(TAG, "Register: failed");
        }
        Log.d(TAG, "Register: id:"+id);
        return id;//-1 = failed
    }
    //get city id
    public static int getCityId(String city_name){
        RequestPackage requestPackage=new RequestPackage();
        requestPackage.setEndPoint("http://raduhdd.asuscomm.com:3000/api/bus-tracking/cityid");
        requestPackage.setMethod("GET");
        requestPackage.setParam("city_name",city_name);
        int city=-1;
        try{

            String jsonString=getUrlString(requestPackage);
            JSONObject jsonBody=new JSONObject(jsonString);
            Log.i(TAG, "Received JSON: " + jsonBody.toString());

            if(jsonBody.getString("message").equals("Succes")){
                city=jsonBody.getInt("id");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }catch(JSONException joe) {
            Log.e(TAG, "Failed to parse JSON", joe);
        }
        return city;
    }

    //get groups

    public static ArrayList<ChatGroup>getChatGroups(int pagina, int city_id){
        ArrayList<ChatGroup> groups=new ArrayList<>();
        RequestPackage requestPackage=new RequestPackage();
        requestPackage.setEndPoint("http://raduhdd.asuscomm.com:3000/api/chat");
        requestPackage.setMethod("GET");
        requestPackage.setParam("city_id",String.valueOf(city_id));
        requestPackage.setParam("page",String.valueOf(pagina));
        requestPackage.setParam("limit","10");

        try{
            String jsonString=getUrlString(requestPackage);
            JSONObject jsonObject=new JSONObject(jsonString);
            parseChatGroups(groups,jsonObject);
            int maxPage=jsonObject.getInt("maxPage");
        } catch (IOException e) {
            e.printStackTrace();
        }catch(JSONException joe) {
            Log.e(TAG, "Failed to parse JSON", joe);
        }
        return groups;
    }
    private static void parseChatGroups(ArrayList<ChatGroup> groups,JSONObject jsonBody)throws IOException, JSONException{
        JSONArray groupsJsonArray=jsonBody.getJSONArray("groups");
        for(int i = 0 ; i <groupsJsonArray.length();i++){
            JSONObject groupJsonObject=groupsJsonArray.getJSONObject(i);
            ChatGroup item=new ChatGroup();
            item.setId(groupJsonObject.getInt("id"));
            item.setName(groupJsonObject.getString("name"));
            item.setLanguage(groupJsonObject.getString("language"));
            groups.add(item);
        }
    }

    //upload group

    public static String createGroup(String name,String lang,int city,String token){
        RequestPackage requestPackage=new RequestPackage();
        requestPackage.setEndPoint("http://raduhdd.asuscomm.com:3000/api/chat");
        requestPackage.setMethod("POST");
        requestPackage.setParam("name",name);
        requestPackage.setParam("language",lang);
        requestPackage.setParam("city",String.valueOf(city));
        requestPackage.setToken(token);//din token ia user id
        
        try{
            String jsonString=getUrlString(requestPackage);
            JSONObject jsonBody=new JSONObject(jsonString);
            Log.i(TAG, "Received JSON: " + jsonBody.toString());
            return jsonBody.getString("message");
        }catch(Exception e){
            System.out.println(e.getMessage());e.printStackTrace();
            Log.d(TAG, "group create: failed");
        }
        return "Error Creating Group";
    }

}
