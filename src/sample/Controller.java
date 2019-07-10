package sample;

import com.google.gson.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.awt.Desktop;

public class Controller {
    @FXML private Label lbl_top;
    @FXML private ComboBox place_cgv,theater_cgv;
    @FXML private TabPane tabpane;
    @FXML private ListView CGVmovie_chart,movietime,CGVeventlist;
    @FXML private ImageView eventImage;
    @FXML private Button eventbtn;
    String getcgv=GetCGV();
    JsonParser parser=new JsonParser();
    JsonArray jsonArray=(JsonArray) parser.parse(getcgv);
    // 이벤트 데이터 json으로 퍼오기
    List<String> imagelist=new ArrayList<>();
    List<String> eventarray=new ArrayList<>();
    List<String> linklist=new ArrayList<>();
    public void CGV_on(ActionEvent event){
        tabpane.setStyle("visibility:true");
        lbl_top.setText("CGV의 정보를 불러옵니다");
        String[] local=GetLocal();
        ObservableList<String> list = FXCollections.observableArrayList(local);
        place_cgv.setItems(list);
        //////////////////////////////////// 이제부터 이벤트띄우기
        try {
            Document doc4 = Jsoup.connect("http://www.cgv.co.kr/culture-event/event/#11").execute().parse();
            Elements elements=doc4.select("script");
            String jsonstr=new String();
            for (Element element : elements) {
                if (element.data().contains("jsonData ")) {

                    Pattern pattern = Pattern.compile("jsonData = ([^;]*);");
                    Matcher matcher = pattern.matcher(element.data());
                    if (matcher.find()) {
                        jsonstr=matcher.group(1);
                    } else {
                        jsonstr="No json";
                        System.err.println("No match found!");
                    }
                    break;
                }
            }
            JsonParser even=new JsonParser();
            JsonArray jsondata=(JsonArray) even.parse(jsonstr);
            for(int i=0;i<jsondata.size();i++) {
                JsonObject obj = (JsonObject) jsondata.get(i);
                eventarray.add(obj.get("description").getAsString());
                imagelist.add(obj.get("imageUrl").getAsString());
                linklist.add(obj.get("link").getAsString());
            }
            CGVeventlist.setItems(FXCollections.observableArrayList(eventarray));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void EventImageShow(MouseEvent event){
        eventbtn.setStyle("visibility:true");
        int num=CGVeventlist.getSelectionModel().getSelectedIndex();
        Image image=new Image(imagelist.get(num));
        eventImage.setImage(image);
    }
    public void Local_select_CGV(ActionEvent event){
        String[] theater=GetTheater();
        ObservableList<String> list = FXCollections.observableArrayList(theater);
        theater_cgv.setItems(list);
    }
    public void Search_CGV(ActionEvent event) {
        int index1=place_cgv.getSelectionModel().getSelectedIndex();
        int index2=theater_cgv.getSelectionModel().getSelectedIndex();
        try {
            String[] li = GetMovieList(index1, index2);
            ObservableList<String> movies = FXCollections.observableArrayList(li);
            CGVmovie_chart.setItems(movies);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void CGVtime(MouseEvent event){
        int index=CGVmovie_chart.getSelectionModel().getSelectedIndex();
        int index1=place_cgv.getSelectionModel().getSelectedIndex();
        int index2=theater_cgv.getSelectionModel().getSelectedIndex();
        try {
            String[] timeleft = GetmovieTime(index1, index2, index);
            String[] seatleft = GetLeft(index1, index2, index);
            String[] leftall=new String[timeleft.length];
            //정렬
            for(int i=0;i<timeleft.length;i++) {
                leftall[i] = timeleft[i] + "            |     " + seatleft[i];
            }
            Arrays.sort(leftall);
            ObservableList<String> newli = FXCollections.observableArrayList(Arrays.asList(leftall));
            movietime.setItems(newli);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void hyperlink(ActionEvent event){
        int num=CGVeventlist.getSelectionModel().getSelectedIndex();
        String a=linklist.get(num).substring(2,linklist.get(num).length());
        String url="http://www.cgv.co.kr/culture-event/event/"+a;
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    private String[] GetLeft(int index1, int index2, int index) throws IOException {
        String theatercode=GetTheatercode(index1,index2);
        String url = "http://www.cgv.co.kr/common/showtimes/iframeTheater.aspx?theatercode=" + theatercode;
        Document doc3 = Jsoup.connect(url).execute().parse();
        String getli="li:eq("+index+")";
        Elements element=doc3.select(".sect-showtimes").select(getli).select(".info-timetable").select("li span");
        String[] array=new String[element.size()];

        for(int i=0;i<element.size();i++){
            array[i]=element.get(i).text();
            array[i]=array[i].replace("잔여좌석","");
        }
        ArrayList<String> new_li= new ArrayList<>();
        for(String x:array) if (!x.equals("")) new_li.add(x);
        String[] new_array=new_li.toArray(new String[new_li.size()]);

        return new_array;
    }
//    cgv상영관 데이터를 JSON형식으로 받음
    private String GetCGV(){
        try {
            Document doc4 = Jsoup.connect("http://www.cgv.co.kr/reserve/show-times/").execute().parse();
            Elements elements=doc4.select("script");
            for (Element element : elements) {
                if (element.data().contains("theaterJsonData")) {

                    Pattern pattern = Pattern.compile("theaterJsonData = ([^;]*);");
                    Matcher matcher = pattern.matcher(element.data());
                    if (matcher.find()) {
                        return matcher.group(1);
                    } else {
                        System.err.println("No match found!");
                    }
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }
    private String[] GetmovieTime(int index1,int index2,int index) throws IOException {
        String theatercode=GetTheatercode(index1,index2);
        String url = "http://www.cgv.co.kr/common/showtimes/iframeTheater.aspx?theatercode=" + theatercode;
        Document doc1 = Jsoup.connect(url).execute().parse();
        String getli="li:eq("+index+")";
        Elements element=doc1.select(".sect-showtimes").select(getli).select(".info-timetable").select("em");
        String[] array=new String[element.size()];
        for(int i=0;i<element.size();i++){
            array[i]=element.get(i).text();

        }
        return array;
    }
    private String GetTheatercode(int index1,int index2){
        JsonObject obj=(JsonObject)jsonArray.get(index1);
        JsonArray detail=obj.get("AreaTheaterDetailList").getAsJsonArray();
        JsonObject AreaTheaterDetailList=(JsonObject)detail.get(index2);
        String theatercode=AreaTheaterDetailList.get("TheaterCode").getAsString();
        return theatercode;
    }
    private String[] GetTheater(){
        int num=place_cgv.getSelectionModel().getSelectedIndex();
        JsonObject obj=(JsonObject)jsonArray.get(num);
        JsonArray detail=obj.get("AreaTheaterDetailList").getAsJsonArray();
        String[] theater=new String[detail.size()];
        for(int i=0;i<detail.size();i++){
            JsonObject object = (JsonObject) detail.get(i);
            theater[i] = object.get("TheaterName").getAsString();
        }
        return theater;
    }
    private String[] GetLocal(){
        String[] local=new String[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject object = (JsonObject) jsonArray.get(i);
                local[i] = object.get("RegionName").getAsString();
        }
        return local;
    }
    private String[] GetMovieList(int index1,int index2) throws IOException {
        String theatercode=GetTheatercode(index1,index2);
        String url = "http://www.cgv.co.kr/common/showtimes/iframeTheater.aspx?theatercode=" + theatercode;
        Document doc2 = Jsoup.connect(url).execute().parse();
        Elements element=doc2.select("[target=_parent]").select("strong");
        String[] array=new String[element.size()];
        for(int i=0;i<element.size();i++){
            array[i]=element.get(i).text();
        }
        return array;
}
}
