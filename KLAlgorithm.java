// KLAlgorithm.java
// Created by Jose Fraga on November 20, 2018 12:22PM

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class KLAlgorithm 
{
    // Data structures
    ArrayList<Double> netWeights;
    LinkedList<int[]> connections;
    LinkedList<double[]> dataZero;
    LinkedList<double[]> dataOne;
    ArrayList<Integer> partitionZero;
    ArrayList<Integer> partitionOne;
    ArrayList<Integer> accounted;
    ArrayList<Integer> order;
    ArrayList<Double> weights;
    // Variables
    long readHgrTime;
    long writeTime;
    long readTime;
    long passTime;
    String saveFileName;
    
    // Constructor
    KLAlgorithm()
    {
        netWeights = new ArrayList<Double>();
        connections = new LinkedList<int[]>();
        dataZero = new LinkedList<double[]>();
        dataOne = new LinkedList<double[]>();
        partitionZero = new ArrayList<Integer>();
        partitionOne = new ArrayList<Integer>();
        accounted = new ArrayList<Integer>();
        order = new ArrayList<Integer>();
        weights = new ArrayList<Double>();
        readHgrTime = 0;
        writeTime = 0;
        readTime = 0;
        passTime = 0;
        saveFileName = null;
    }
    
    public void readHgrFile(String fileName)
    {
        // Computation time begin
        long start = System.nanoTime();

        try
        {
            // Obtain input bytes from the .hgr file.
            FileInputStream stream = new FileInputStream(fileName);
            // Read the input stream.
            DataInputStream dataIn = new DataInputStream(stream);
            // Wrap a BufferedReader around the Reader.
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(dataIn));
            String line;
            
            // Skip the first line of information.
            String firstLine = buffRead.readLine();
            
            // Store the connections
            while ((line = buffRead.readLine()) != null)
            {
                // Split this string around matches of the given regular expression.
                String[] lineString = line.split(" ");

                // Calculate edge weight of a net with M connections (i.e., # of nodes)
                double M = lineString.length;
                double weight = 0;
                if (M > 1)
                {
                    weight = 1 / (M - 1);
                }
                
                netWeights.add(weight);
                
                int[] tempArray = new int[lineString.length];
                for (int i = 0; i < lineString.length; i++)
                {   
                    String node = lineString[i];
                    // Convert String to int
                    int nodeInt = Integer.parseInt(node);
                    // Add to the temporary int[]
                    tempArray[i] = nodeInt;
                }

                // Add the connection to the LinkedList
                connections.add(tempArray);
            }

            // Close the file
            dataIn.close();
        }
        catch (IOException e)              
        {
            e.printStackTrace();
        }
        
        // End computation time
        long end = System.nanoTime();
        readHgrTime = (end - start);        
    }
    
    public void writeFile()
    {
        // Computation time begin
        long start = System.nanoTime();
        
        // Save the partition that you generated to a .part file
        try
        {
            String partName = saveFileName.replaceAll(".hgr", ".part");
            FileWriter write = new FileWriter(partName);
            BufferedWriter buffWrite = new BufferedWriter(write);

            // partitionZero
            Iterator<Integer> iterateZero = partitionZero.iterator();
            while (iterateZero.hasNext())
            {                
                Object next = iterateZero.next();
                // Convert Object to String
                String node = String.valueOf(next);
                // Write the String
                buffWrite.write(node);
                buffWrite.append(" ");
                // The partition
                buffWrite.append("0");
                // Next node
                buffWrite.newLine();
            }
            
            // partitionOne
            Iterator<Integer> iterateOne = partitionOne.iterator();
            while (iterateOne.hasNext())
            {                
                Object next = iterateOne.next();
                // Convert Object to String
                String node = String.valueOf(next);
                // Write the String
                buffWrite.write(node);
                buffWrite.append(" ");
                // The partition
                buffWrite.append("1");
                // Next node
                buffWrite.newLine();
            }            
            
            // Close the file
            buffWrite.close();
        }
        catch (IOException exception) 
        {
            exception.printStackTrace();
        }
        
        // End computation time
        long end = System.nanoTime();
        writeTime = (end - start);
    }    
    
    // Read from the .part file or create a .part file and save.
    public void readPartFile(String fileName, String fileType)
    {
        // Computation time begin
        long start = System.nanoTime();
        try 
        {
            // Obtain input bytes from the file.
            FileInputStream stream = new FileInputStream(fileName);
            // Read the input stream.
            DataInputStream dataIn = new DataInputStream(stream);
            // Wrap a BufferedReader around the Reader.
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(dataIn));
            String line;           
            
            // We have the partition already provided
            if (fileType == "part")
            {
                // Add partioned nodes to their appropriate partition
                while ((line = buffRead.readLine()) != null)
                {
                    // Split this string around matches of the given regular expression.
                    String[] lineString = line.split(" ");                  

                    String partString = lineString[1];
                    // Convert String to int
                    int partition = Integer.parseInt(partString);
                    
                    // Store the node in the appropriate partition
                    if (partition == 0)
                    {                       
                        String node = lineString[0];
                        // Convert String to int
                        int nodeInt = Integer.parseInt(node);
                        // Add the integer to the partition
                        partitionZero.add(nodeInt);
                    }
                    else if (partition == 1)
                    {
                        String node = lineString[0];
                        // Convert String to int
                        int nodeInt = Integer.parseInt(node);
                        // Add the integer to the partition
                        partitionOne.add(nodeInt);
                    }
                }
            }
            // We have the hypergraph only, partition the nodes
            else
            {                
                // Store the nodes
                ArrayList<Integer> temp = new ArrayList<Integer>();
                while((line = buffRead.readLine()) != null)
                {          
                    // Split this string around matches of the given regular expression.
                    String[] lineString = line.split(" ");
                    for (int i = 0; i < lineString.length; i++)
                    {
                        String node = lineString[i];
                        // Convert String to int
                        int nodeInt = Integer.parseInt(node);
                        temp.add(nodeInt);
                    }
                }                
                
                // Add into a Set to "remove" duplicates.
                Set<Integer> set = new HashSet<>();
                set.addAll(temp);
                temp.clear();
                temp.addAll(set);
                
                // Partition the nodes
                int median = (temp.size() / 2);
                int last = temp.size();
                List<Integer> firstHalf = temp.subList(0, median);
                List<Integer> secondHalf = temp.subList(median, last);
                
                // Store the first half of the nodes in partition 0
                partitionZero.addAll(firstHalf);
                // Store the second half of the nodes in partition 1
                partitionOne.addAll(secondHalf);  
                
                // Close the file
                dataIn.close();
                
                // Write the generated partition to a .part file
                writeFile();
            }
        }
        catch (IOException e)              
        {
            e.printStackTrace();
        }
        
        // End computation time
        long end = System.nanoTime();
        readTime = (end - start);
    }    
    
    // Function to create a data structure to consolidate all pertinent information.
    // To store node, status, move cost, of each node. At the moment only the node is
    // being added to the data structure. Other information will be added as needed.
    public void linkData()
    {        
        // Get partition 0 nodes
        for (int x = 0; x < partitionZero.size(); x++)
        {
            // Create an array of three elements to store node, status, move cost, 
            // in that order.
            double[] data = new double[3];            
            // Store the node at index 0
            data[0] = partitionZero.get(x);            
            dataZero.add(data);
        }
        // Get partition 1 nodes
        for (int x = 0; x < partitionOne.size(); x++)
        {
            // Create an array of three elements to store node, status, move cost,
            // in that order.
            double[] data = new double[3];            
            // Store the node at index 0
            data[0] = partitionOne.get(x);
            dataOne.add(data);
        }        
    }
    
    public boolean contains(int partition, int node, String type)
    {       
        // External
        if (type == "external")
        {
            if (partition == 0)
            {                
                return partitionOne.contains(node);
            }
            else if (partition == 1)
            {
                return partitionZero.contains(node);
            }
        }
        // Internal
        else if (type == "internal")
        {
            if (partition == 0)
            {
                return !(partitionOne.contains(node));
            }
            else if (partition == 1)
            {
                return !(partitionZero.contains(node));
            }            
        }
        
        return false;
    }    
    
    // Connectivity graph connections
    public double graphConnections(int netIndex, int connectArrayIndex, 
            int[] connectArray, int partition, String type)
    {
        // Get the weight of the appropriate net
        double weight = netWeights.get(netIndex);
        
        double connection = 0;
        // Node is the first element in connectArray
        if (connectArrayIndex == 0)
        {
            // Check right
            int nodeRight = connectArray[connectArrayIndex + 1];            
            boolean containsRight = contains(partition, nodeRight, type);           
            
            if (containsRight && !(accounted.contains(nodeRight)))
            {
                // Connection found
                connection = connection + weight;
                
                accounted.add(nodeRight);
            }
            
            // Check last
            int lastIndex = connectArray.length - 1;
            int lastNode = connectArray[lastIndex];
            boolean containsLast = contains(partition, lastNode, type);
            
            if (containsLast && !(accounted.contains(lastNode)))
            {
                // Connection found
                connection = connection + weight;
                
                accounted.add(lastNode);               
            }
        }
        // Node is the last element in connectArray
        else if (connectArrayIndex == (connectArray.length - 1))
        {
            // Check left
            int nodeLeft = connectArray[connectArrayIndex - 1];            
            boolean containsLeft = contains(partition, nodeLeft, type);
            
            if (containsLeft && !(accounted.contains(nodeLeft)))
            {
                // Connection found
                connection = connection + weight;

                accounted.add(nodeLeft);                
            }
            
            // Check first
            int firstNode = connectArray[0];
            boolean containsFirst = contains(partition, firstNode, type);
            
            if (containsFirst && !(accounted.contains(firstNode)))
            {
                // Connection found
                connection = connection + weight;
                
                accounted.add(firstNode);               
            }
        }
        // Nodes in the middle. Check to the left and right.
        else
        {
            int nodeRight = connectArray[connectArrayIndex + 1];
            int nodeLeft = connectArray[connectArrayIndex - 1];
            boolean containsRight = contains(partition, nodeRight, type);
            boolean containsLeft = contains(partition, nodeLeft, type);
            
            if (containsRight && !(accounted.contains(nodeRight)))
            {
                // Connection found
                connection = connection + weight;
                
                accounted.add(nodeRight);                
            }
            
            if (containsLeft && !(accounted.contains(nodeLeft)))
            {
                // Connection found
                connection = connection + weight;
                
                accounted.add(nodeLeft);               
            }
        }
        return connection;        
    }
    
    // Calculate move cost of each node by finding external and internal connection values.
    public double costDv(double node, int partition)
    {
        // New node
        accounted.clear();
        
        double Ev = 0;
        double Iv = 0;
        double totalEv = 0.0;
        double totalIv = 0.0;
        // Loop through the whole LinkedList (connections) of nets
        for (int i = 0; i < connections.size(); i++)
        {
            // Get the int[] array (net) at the specified index i
            int[] connectArray = connections.get(i);

            // Loop through the int[] array (net)
            for (int x = 0; x < connectArray.length; x++)
            {   
                // Find connections
                if (connectArray[x] == node)
                {
                    // Find external connections
                    String out = "external";
                    Ev = graphConnections(i, x, connectArray, partition, out);
                    
                    // Multiple occurences
                    totalEv = totalEv + Ev;
                    
                    // Find internal connections
                    String in = "internal";
                    Iv = graphConnections(i, x, connectArray, partition, in);
                    
                    // Multiple occurences
                    totalIv = totalIv + Iv;                  
                }
            }
        }
        
        // Calculate D(v) = |E(v)| - |I(v)|
        double Dv = totalEv - totalIv;
        return Dv;
    }    

    // Calculate the move cost for all nodes and store the value.
    public void allCostDv()
    {
        // Compute move cost for each node.
        for (int i = 0; i < dataZero.size(); i++)
        {
            double[] data = dataZero.get(i);
            
            // Read data[0] to get the node
            double node = data[0];
            int partition = 0;
            double moveCost = costDv(node, partition);
            
            // Store the move cost.
            data[2] = moveCost;
            // Remove old data
            dataZero.remove(i);
            // Replace old data with new data
            dataZero.add(i, data);
        }
        for (int i = 0; i < dataOne.size(); i++)
        {
            double[] data = dataOne.get(i);
            
            // Read data[0] to get the node
            double node = data[0];
            int partition = 1;
            double moveCost = costDv(node, partition);

            // Store the move cost.
            data[2] = moveCost;
            // Remove old data
            dataOne.remove(i);
            // Replace old data with new data
            dataOne.add(i, data);
        }        
    }
    
    public void swapCells(double nodeX, double nodeY, int x, int y)
    {
        // Swap cell from partition 0 to partition 1
        // Remove
        double[] partZero = dataZero.remove(x);
        // Mark cell as fixed (1 = FIXED)
        partZero[1] = 1;
        // Include
        dataOne.add(partZero);

        // Update retrieval data structure
        // Convert double to int
        int removeX = (int) nodeX;
        // Convert int to Object
        Object removeZero = (Integer) removeX;
        // Remove the element removeZero, not the element
        // at index removeZero.
        partitionZero.remove(removeZero);
        // Add the integer
        partitionOne.add(removeX);

        // Keep track of swapped cells
        // ADD(order,(delta g[i],(a[i],b[i])))
        order.add(removeX);                                        

        // Swap cell from partition 1 to partition 0
        double[] partOne = dataOne.remove(y);
        // Mark cell as fixed (1 = FIXED)
        partOne[1] = 1;
        // Include
        dataZero.add(partOne);

        // Update retrieval data structure
        // Convert double to int
        int removeY = (int) nodeY;
        // Convert int to Object
        Object removeOne = (Integer) removeY;
        // Remove the element removeOne, not the
        // element at index removeOne
        partitionOne.remove(removeOne);
        // Add the integer
        partitionZero.add(removeY);

        // Keep track of swapped cells
        // ADD(order,(delta g[i],(a[i],b[i])))
        order.add(removeY);
    }

    public void maxSwapGain()
    {
        // Variables
        double maxGain = 0;
        int setMaxGain = 0;
        double maxNodeX = 0;
        double maxNodeY = 0;
        int maxIndexX = 0;
        int maxIndexY = 0;
        int index = 0;
        
        // Iterate dataZero
        for (int x = 0; x < dataZero.size(); x++)
        {
            double[] zeroData = dataZero.get(x);

            // Check that individual cell is free. Function checkFree() checks
            // if there are free cells at all, irrespective of individual cells.
            if (zeroData[1] == -1)
            {           
                // Read data[0] to get the node
                double nodeX = zeroData[0];              
   
                // Iterate dataOne
                for (int y = 0; y < dataOne.size(); y++)
                {                               
                    double[] oneData = dataOne.get(y);

                    // Check that cell is free.
                    if (oneData[1] == -1)
                    {                                  
                        // Read data[0] to get the node
                        double nodeY = oneData[0];

                        // Move cost of nodeX
                        double Dx = zeroData[2];
                        // Move cost of nodeY
                        double Dy = oneData[2];                       
                        
                        double weight = weights.get(index);                      

                        // Calculate delta gain = D(x) + D(y) - 2 * c(x,y)
                        double gain = Dx + Dy - 2 * weight;

                        // Initialize maxGain as the first available gain, rather
                        // than using the initialization of 0 above. This
                        // accounts for negative values.
                        if (setMaxGain == 0)
                        {
                            maxGain = gain;
                            maxNodeX = nodeX;
                            maxNodeY = nodeY;
                            maxIndexX = x;
                            maxIndexY = y;
                            setMaxGain = 1;                           
                        }

                        // Alternative to storing all max values and then
                        // searching for the max value. Simply find the max
                        // value and nodes. Will always take the first max gain
                        // if there is a tie.
                        if (gain > maxGain)
                        {
                            maxGain = gain;
                            maxNodeX = nodeX;
                            maxNodeY = nodeY;
                            maxIndexX = x;
                            maxIndexY = y;
                        }
                    }                  
                    index++;
                }
            }
        }
        
        // Swap cells with the max gain.
        swapCells(maxNodeX, maxNodeY, maxIndexX, maxIndexY);
    }
    
    // Function is passed the net of the x-coordinate from the pair. Depending
    // on the location of the x-coordinate node in the net, the function will
    // compare the immediately surrounding nodes to the y-coordinate node (pairNodeY).
    // If an immediately surrounding node equals pairNodeY, then obviously there
    // is a connection weight.
    public double connectionWeight(double pairNodeY, int netIndex,
            int connectArrayIndex, int[] connectArrayX)
    {        
        // Get the weight of the appropriate net
        double weight = netWeights.get(netIndex);
        
        double connectWeight = 0;
        // Node is the first element in connectArray
        if (connectArrayIndex == 0)
        {
            // Check right
            int nodeRight = connectArrayX[connectArrayIndex + 1];          
            
            if (nodeRight == pairNodeY)
            {
                // Weight connection found
                connectWeight = weight;
            }
            
            // Check last
            int lastIndex = connectArrayX.length - 1;
            int lastNode = connectArrayX[lastIndex];
            
            if (lastNode == pairNodeY)
            {
                // Weight connection found
                connectWeight = weight;
            }
        }
        // Node is the last element in connectArray
        else if (connectArrayIndex == (connectArrayX.length - 1))
        {
            // Check left
            int nodeLeft = connectArrayX[connectArrayIndex - 1];
            
            if (nodeLeft == pairNodeY)
            {
                // Weight connection found
                connectWeight = weight;
            }
            
            // Check first
            int firstNode = connectArrayX[0];
            
            if (firstNode == pairNodeY)
            {
                // Weight connection found
                connectWeight = weight;                
            }
        }
        // Nodes in the middle. Check to the left and right.
        else
        {
            int nodeRight = connectArrayX[connectArrayIndex + 1];
            int nodeLeft = connectArrayX[connectArrayIndex - 1];
            
            if (nodeRight == pairNodeY)
            {
                // Weight connection found
                connectWeight = weight;
            }
            
            if (nodeLeft == pairNodeY)
            {
                // Weight connection found
                connectWeight = weight;
            }
        }
        
        return connectWeight;
    }
    
    // Initialize weight data structure to represent the connection weight
    // between pair (x,y). The size of weights data structure will be the number
    // of all pair combinations.
    public void initializeWeight()
    {
        weights.clear();
        
        // Variable
        double initialize = 0;
        
        // Iterate dataZero
        for (int x = 0; x < dataZero.size(); x++)
        {            
            // Iterate dataOne
            for (int y = 0; y < dataOne.size(); y++)
            {
                weights.add(initialize);
            }
        }
    }
    
    // Update the connection weight of the pertaining nodes if the current connection
    // weight is a greater value than the one previously stored.
    public void storeConnectWeight(double nodeX, double nodeY, double weight)
    {
        int index = 0;
       
        // Iterate dataZero
        for (int x = 0; x < dataZero.size(); x++)
        {
            double[] zeroData = dataZero.get(x);
            // Read data[0] to get the node
            double tempNodeX = zeroData[0];

            // Iterate dataOne
            for (int y = 0; y < dataOne.size(); y++)
            {
                double[] oneData = dataOne.get(y);
                // Read data[0] to get the node
                double tempNodeY = oneData[0];

                // Compare to assure we are updating the correct value in weights
                // data structure.
                if ((tempNodeX == nodeX) && (tempNodeY == nodeY))
                {
                    // Remove the previous connection weight value.
                    weights.remove(index);
                    // Add the current connection weight in the same location.
                    weights.add(index, weight);
                }

                index++;
            }
        }
    }
    
    public void allConnectionWeights()
    {
        // Variables
        double nodeX = 0;
        double nodeY = 0;
        int index = 0;
        
        // Iterate dataZero
        for (int x = 0; x < dataZero.size(); x++)
        {
            double[] zeroData = dataZero.get(x);         
            // Read data[0] to get the node
            nodeX = zeroData[0];

            // Loop through the LinkedList (connections) of nets
            for (int a = 0; a < connections.size(); a++)
            {
                // Get the int[] array (net) at the specified index i
                int[] connectArray = connections.get(a);

                // Loop through the int[] array (net)
                for (int b = 0; b < connectArray.length; b++)
                {   
                    // Find connections
                    if (connectArray[b] == nodeX)
                    {
                        // Iterate dataOne
                        for (int y = 0; y < dataOne.size(); y++)
                        {                               
                            double[] oneData = dataOne.get(y);
                            // Read data[0] to get the node
                            nodeY = oneData[0];

                            double weight = connectionWeight(nodeY, a, b, connectArray);

                            // Basically if weight = 0, then no connection weight.
                            // Keep the 0 in weights data structure.
                            if (weight > 0)
                            {
                                storeConnectWeight(nodeX, nodeY, weight);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setFree()
    {   
        for (int i = 0; i < dataZero.size(); i++)
        {
            double[] data = dataZero.get(i);
            
            // Set node as FREE where (-1 = FREE)
            data[1] = -1;
            // Remove old data
            dataZero.remove(i);
            // Replace old data with new data
            dataZero.add(i, data);
        }
        for (int i = 0; i < dataOne.size(); i++)
        {
            double[] data = dataOne.get(i);
            
            // Set node as FREE where (-1 = FREE)
            data[1] = -1;
            // Remove old data
            dataOne.remove(i);
            // Replace old data with new data
            dataOne.add(i, data);            
        }
    }
    
    // Iterate through dataZero and dataOne. If you find -1, then FREE cell
    public boolean checkFree()
    {
        for (int i = 0; i < dataZero.size(); i++)
        {
            // Get the int[] array at the specified index i
            double[] dataArray = dataZero.get(i);
            double status = dataArray[1];

            // -1 = FREE
            if (status == -1)
            {
                return true;
            }
        }
        for (int i = 0; i < dataOne.size(); i++)
        {
            // Get the int[] array at the specified index i
            double[] dataArray = dataOne.get(i);
            double status = dataArray[1];

            // -1 = FREE
            if (status == -1)
            {
                return true;
            }                    
        } 
        
        // Not a single FREE cell found
        return false;
    }
    
    // Input: graph G(V,E) with |V| = 2n
    // Output: partitioned graph G(V,E)
    public void algorithm(int passes)
    {
        // Set each node as free (-1 = FREE)
        setFree();
        
        int passCount = 1;
        while (passes > 0)
        {
            // Computation time begin
            long start = System.nanoTime();

            // Compute and update D(v) for each node.
            allCostDv();
            
            // Calculate connection weight between pairs.
            initializeWeight();
            allConnectionWeights();
            
            // Initial check for free cells.
            boolean free = checkFree();
            
            // While there are free cells, find
            if (free)
            {
                // While balance (size partition 0 == size partition 1) is 
                // maintained, continue...
                int areaA = partitionZero.size();
                int areaB = partitionOne.size();
                if (areaA == areaB)
                {
                    maxSwapGain();
                }
                else
                {
                    // testing...
                    System.out.println("Unbalanced partition. Pass incomplete. Done.");
                            
                    // Override
                    free = false;
                    passes = -1;
                }
            }
            else
            {
                // testing...
                System.out.println("No more free cells to swap. Done.");
                
                // Override
                free = false;
                passes = -1;
            }

            passes = passes - 1;
            
            // End computation time
            long end = System.nanoTime();
            passTime = (end - start);
            
            System.out.println("Pass " + passCount + " runtime: " + passTime + "ns");
            passCount = passCount + 1;           
        }
    }
    
    // Three arguments (in this order), .hgr file (graph G(V,E) with |V|=2n), 
    // max number of passes, optional .part file (initial partition (graph G(V,E) with |V|=2n))
    public void algorithmInput(String fileName, int passes, String fileNameTwo)
    {   
        saveFileName = fileName;
        
        // If a .part file was provided (i.e., graph partitioned)
        // use it, else use other file.
        if (fileNameTwo != null)
        {
            readHgrFile(fileName);
            String partFile = "part";
            readPartFile(fileNameTwo, partFile);
            // Consolidate all information into one data structure
            linkData();
            algorithm(passes);
        }
        // Read .hgr file (i.e., graph is not partioned, needs partitioning)
        else
        {
            readHgrFile(fileName);
            String hgrFile = "hgr";
            readPartFile(fileName, hgrFile);
            // Consolidate all information into one data structure
            linkData();
            algorithm(passes);
        }
    }
    
    public static void main(String[] args)
    {
        // Runtime computation time begin
        long start = System.nanoTime();
        
        // Class declaration
        KLAlgorithm klClass = new KLAlgorithm();

        // Read user input
        Scanner input = new Scanner(System.in);
        System.out.println("Enter .hgr file.");
        String hgrFile = input.nextLine();
        System.out.println("Enter the number of passes.");
        Scanner inputPass = new Scanner(System.in);
        int passes = inputPass.nextInt();
        System.out.println("Do you have a .part file to provide yes/no?");
        String answer = input.nextLine();

        if ("yes".equals(answer))
        {
            System.out.println("Enter .part file.");
            String partFile = input.nextLine();
            System.out.println();
            System.out.println("Running...");
            klClass.algorithmInput(hgrFile, passes, partFile);
            
            System.out.println("\nRead .part file runtime: " + klClass.readTime + "ns\n");           
        }
        else
        {
            System.out.println();
            System.out.println("Running...");
            // .part file not provided
            klClass.algorithmInput(hgrFile, passes, null);
            
            System.out.println("\n'.part' file was created and saved.");
            System.out.println("Write file runtime: " + klClass.writeTime + "ns\n");
        }
        
        // Swap sequence that maximizes Gm
        System.out.println("Swap sequence of cells that maximizes Gm:");
        System.out.println(klClass.order.toString());
        
        // End runtime computation time
        long end = System.nanoTime();
        long time = (end - start);

        // Print runtime
        /*System.out.println("\nRead .hgr file runtime: " + klClass.readHgrTime + "ns");
        
        long readTotalTime = klClass.readHgrTime + klClass.readTime + time;
        long writeTotalTime = klClass.readHgrTime + klClass.writeTime + time;
        
        if ("yes".equals(answer))
        {
            System.out.println("\nComputation time: " + readTotalTime + "ns\n");
        }
        else
        {
            System.out.println("\nComputation time: " + writeTotalTime + "ns\n");
        }*/
        
        System.out.println("\nComputation time: " + time + "ns\n");
    }
}