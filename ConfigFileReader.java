/*
   This class will be used to periodically read the configuration file. 
   The configuration file's purpose is to update us on whether mobile users 
   have moved. 

   It needs to be capable of returning the necessary information from the 
   configuration file, so that, we can properly update the mobile user on it's 
   new location and neighbors

   Note: each mobile user will have access to the same configuration file due to 
   the set-up of Auburn University's H-drive system. 

*/

import java.util.*;
import java.net.*;
import java.io.*;

/**
Reads configFile and returns the information requested.
*/
public class ConfigFileReader{

   public Scanner reader; //Scanner to read config file.
   public File configFile;
/**
Constructor for configFileReader object
@param filename - String representation of the path to the configFile.
*/
   public ConfigFileReader(String filename){
      try{
         configFile = new File(filename);
         reader = new Scanner(configFile);//sets the scanner to read the config file.
      }
      catch(Exception e){//catches fileNotFound exception.
         System.out.println("File not found.");
      }
   }
   
   /**
   gets the port for the desired node.
   @param target - number of the node of which to get the port.
   @return port - the port of the desired node.
   */
   public int getPort(int target){
      String line = "";
      int node;
      int port = 0;
      String[] tokens;
      do{
         line = reader.nextLine();
         tokens = line.split(" ");//tokenize the line.
         tokens[2] = tokens[2].substring(0,6);//trim the comma off of the host name.
         node = Integer.parseInt(tokens[1]);// get the node from the line of the config file.
      }while(node != target);//check to see if the desired node has been found.
      port = Integer.parseInt(tokens[3]);//set the port to be returned.
      reset();
      return port;
   }
   
   /**
   gets the coordinates of the desired node.
   @param target - number of the node of which to get the coordinates.
   @return port - the coordinates of the desired node.
   */
   public int[] getCoordinates(int target){
      String line = "";
      int node;
      int[] coordinates = {0,0};
      String[] tokens;
      do{
         line = reader.nextLine();
         tokens = line.split(" ");//tokenize the line.
         tokens[2] = tokens[2].substring(0,6);//trim the comma off of the host name.
         node = Integer.parseInt(tokens[1]);// get the node from the line of the config file.
      }while(node != target);//check to see if the desired node has been found.
      coordinates[0] = Integer.parseInt(tokens[4]);//set x coordinate.
      coordinates[1] = Integer.parseInt(tokens[5]);//set y coordinate.
      reset();
      return coordinates;
   }
   
   /**
   gets the InetAddress of the desired node.
   @param target - number of the node of which to get the InetAddress.
   @return ip - the InetAddress of the desired node.
   @return null - if the host name is bad.
   */
   public int[] getNeighbors(int target){
      String line = "";
      int node;
      int[] neighbors;
      String[] tokens;
      
      do{
         line = reader.nextLine();
         tokens = line.split(" ");//tokenize the line.
         tokens[2] = tokens[2].substring(0,6);//trim the comma off of the host name.
         node = Integer.parseInt(tokens[1]);// get the node from the line of the config file.
      }while(node != target && reader.hasNextLine());//check to see if the desired node has been found.
      
      int numNeighbors = tokens.length - 7;//calculate the number of neighbors.
      int index = 7;//index of first neighbor.
      neighbors = new int[numNeighbors];//set the size of the neighbor array.
      
      for(int i = 0; i < numNeighbors; i++){
         neighbors[i] = Integer.parseInt(tokens[index]);//add neighbor to the array.
         index++;//go to the next neighbor.
      }
      
      reset();
      return neighbors;
   }
   
   public int[] update(int target){
      String line = "";
      int node;
      int[] data;
      String[] tokens;
      
      do{
         line = reader.nextLine();
         tokens = line.split(" ");//tokenize the line.
         tokens[2] = tokens[2].substring(0,6);//trim the comma off of the host name.
         node = Integer.parseInt(tokens[1]);// get the node from the line of the config file.
      }while(node != target && reader.hasNextLine());//check to see if the desired node has been found.
      
      int entries = tokens.length - 5;//calculate the number of entries.
      int index = 7;//index of first neighbor.
      data = new int[entries];//set the size of the data array.
      data[0] = Integer.parseInt(tokens[4]);//set x coordinate.
      data[1] = Integer.parseInt(tokens[5]);//set y coordinate.
      
      for(int i = 2; i < entries; i++){
         data[i] = Integer.parseInt(tokens[index]);//add neighbor to the array.
         index++;//go to the next neighbor.
      }
      reset();
      return data;
   }
   
   public InetAddress getIP(int target){
      String line = "";
      int node;
      int[] neighbors;
      String[] tokens;
      do{
         line = reader.nextLine();
         tokens = line.split(" ");//tokenize the line.
         tokens[2] = tokens[2].substring(0,6);//trim the comma off of the host name.
         node = Integer.parseInt(tokens[1]);// get the node from the line of the config file.
      }while(node != target && reader.hasNextLine());//check to see if the desired node has been found.
      
      reset();
      try{
         InetAddress ip = InetAddress.getByName(tokens[2]);
         return ip;
      }
      
      catch(Exception e){
         System.out.println("Bad host name: " + tokens[2]);
      }
      return null;
   }

   public void reset(){
   try{
       reader = new Scanner(configFile);//resets the scanner to read the config file.
       }
       catch(Exception e){
       System.out.println("File not found");
       }
   }
}




