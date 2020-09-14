package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Main {

    public static void main(String[] args) {
        String url = "jdbc:postgresql://127.0.0.1:";//5433/plant
        String user,pass,port,Sfile,nameDB,maxUrl; //"postgres"; //"root";
        Boolean endless = true;
        Connection connect = null;

        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите порт БД: ");port = scanner.next();
        System.out.print("Введите имя БД: ");nameDB = scanner.next();
        System.out.print("Введите имя пользователя: "); user = scanner.next();
        System.out.print("Введите пароль: ");pass = scanner.next();

        while(endless == true){
            endless = false;
            System.out.print("Введите локацию XML-файла: ");Sfile = scanner.next();
            maxUrl = url+port+"/"+nameDB;System.out.println(maxUrl);
            File file = new File(Sfile); //     C:\Users\1\workspace\maven\src\main\resources\data\plants__000.xml

            try {
                Class.forName("org.postgresql.Driver");
                connect = DriverManager.getConnection(maxUrl, user, pass);

                System.out.println();System.out.println("===================");
                if (connect!=null){System.out.println("Connection complete");}else{System.out.println("Connect failed");}
                System.out.println("===================");System.out.println();

                try {
                    String COMMON = null,BOTANICAL= null,ZONE= null,LIGHT= null,PRICE= null,AVAILABILITY= null, uuid = null,date= null,company= null;

                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(file);

                    // Получаем корневой элемент
                    Node root = document.getDocumentElement();

                    //получаем uuid,date,company
                    PreparedStatement drStat;
                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                    NodeList nodeList = document.getElementsByTagName("CATALOG");
                    uuid = nodeList.item(0).getAttributes().getNamedItem("uuid").getNodeValue();System.out.println(uuid);
                    date = nodeList.item(0).getAttributes().getNamedItem("date").getNodeValue();System.out.println(date);Date dated = formatter.parse(date);java.sql.Date sqlDate = new java.sql.Date(dated.getTime());
                    company = nodeList.item(0).getAttributes().getNamedItem("company").getNodeValue();System.out.println(company);
                    drStat = connect.prepareStatement("INSERT INTO d_cat_catalog (id,delivery_date,company,uuid) VALUES (default,?,?,?)");drStat.setDate(1, sqlDate);drStat.setString(2, company);drStat.setString(3, uuid);drStat.execute();

                    PreparedStatement prStat;
                    // Просматриваем все подэлементы корневого - т.е. plants
                    NodeList plants = root.getChildNodes();
                    for (int i = 0; i < plants.getLength(); i++){
                        Node plant = plants.item(i);

                        // Если нода не текст, то это plants - заходим внутрь
                        if (plant.getNodeType() != Node.TEXT_NODE) {
                            NodeList plantProps = plant.getChildNodes();

                            for(int j = 0; j < plantProps.getLength(); j++) {
                                Node plantProp = plantProps.item(j);



                                // Если нода не текст, то это один из параметров - печатаем
                                if (plantProp.getNodeType() != Node.TEXT_NODE) {

                                    if (plantProp.getNodeName()=="COMMON"){COMMON = plantProp.getChildNodes().item(0).getTextContent();System.out.println(COMMON);
                                    }

                                    if (plantProp.getNodeName()=="BOTANICAL"){BOTANICAL = plantProp.getChildNodes().item(0).getTextContent();System.out.println(BOTANICAL);
                                    }

                                    if (plantProp.getNodeName()=="ZONE"){ZONE = plantProp.getChildNodes().item(0).getTextContent();System.out.println(ZONE);
                                    }

                                    if (plantProp.getNodeName()=="LIGHT"){LIGHT = plantProp.getChildNodes().item(0).getTextContent();System.out.println(LIGHT);
                                    }

                                    if (plantProp.getNodeName()=="PRICE"){PRICE = plantProp.getChildNodes().item(0).getTextContent();System.out.println(PRICE.substring(1));
                                    }

                                    if (plantProp.getNodeName()=="AVAILABILITY"){ AVAILABILITY = plantProp.getChildNodes().item(0).getTextContent();System.out.println(AVAILABILITY);
                                    }

                                    if (AVAILABILITY!=null){
                                        prStat = connect.prepareStatement("INSERT INTO f_cat_plants (COMMON,BOTANICAL,ZONE,LIGHT,PRICE,AVAILABILITY,CATALOG_ID) VALUES (?,?,?,?,?,?,(SELECT MAX(id) FROM d_cat_catalog) ) ");prStat.setString(1, COMMON);prStat.setString(2, BOTANICAL);
                                        prStat.setInt(3,Integer.parseInt(ZONE));prStat.setString(4, LIGHT);prStat.setDouble(5, Double.parseDouble(PRICE.substring(1)));prStat.setInt(6,Integer.parseInt(AVAILABILITY));
                                        prStat.execute();AVAILABILITY = null;}
                                }


                            }
                            System.out.println("===========>>>>");
                        }
                    }
                } catch (Exception e1) {e1.printStackTrace();}

            } catch (Exception e) {e.printStackTrace();
            } finally {if (connect != null){try {connect.close();} catch (SQLException e) {e.printStackTrace();}}}
            System.out.println("Хотите ли продолжить загрузку XML?(1-yes/0-no): ");

            Integer Choice = null;
            Choice = scanner.nextInt();
            switch(Choice){
                case 1: endless = true;break;
                case 0: endless = false;break;
            }


        }
        System.out.println();System.out.println("===================");
        System.out.println("Disconnection");
        System.out.println("===================");System.out.println();
    }
}
