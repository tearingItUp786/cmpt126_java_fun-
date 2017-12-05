/**
 *  Assignment Two
 **/

import java.util.Scanner;
import java.io.*;
public class AssignmentTwo {
    /** Classes and Objects **/
    static class Item {
        // Fixed variables
        String originatingOffice;
        String recipient;
        String destinationOffice;

        // Updated variables
        int status = 0;         // 0=inactive, 1=active, 2=accepted, 3=transit, 4=waiting
        int nextAction = 0;
        int enteredSystem = -1;
    }

    static class Pickup {
        String pickupOffice;
        String person;
        int status = 0;
    }

    static class Letter extends Item {
        String returnPickupName = "NONE";
    }

    static class Package extends Item {
        int postage;
        int packageLength;
    }

    static class Office {
        // Fixed variables
        String officeName;
        int transitTime;
        int postage;
        int capacity;
        int persuasion;
        int maxPackageSize;

        // Updated variables
        int currentLoad = 0;

        // Writer variables
        PrintWriter writer;
    }


    /** Methods **/
    public static boolean CriminalCheck (String criminalName, String[] wantedArray) {
        for (String currentName : wantedArray) {
            if (currentName.equals(criminalName)) {
                return true;
            }
        }
        return false;
    }

    public static int OfficeID (String searchName, Office[] officesArray) {
        for (int forPos = 0; forPos != officesArray.length; forPos = forPos + 1) {
            if (searchName.equals(officesArray[forPos].officeName)) {
                return forPos;
            }
        }
        return -1;
    }


    /** Main **/
    public static void main(String[] args) throws FileNotFoundException {
        // Declare variables
            // Reading and writing variables
        String officeFile = "offices.txt";
        String wantedFile = "wanted.txt";
        String commandFile = "commands.txt";

        Scanner officeInput = new Scanner(new File(officeFile));
        Scanner wantedInput = new Scanner(new File(wantedFile));
        Scanner commandInput = new Scanner(new File(commandFile));

        int officeCommands = officeInput.nextInt();
        int wantedCommands = wantedInput.nextInt();
        int commandCommands = commandInput.nextInt();
        commandInput.nextLine(); // This has to happen after the nextInt so we don't miss a line

        PrintWriter masterWriter = new PrintWriter("log_master.txt"); // Creates master log file
        PrintWriter frontWriter = new PrintWriter("log_front.txt"); // Creates front log file

            // Define arrays
        Office[] officesArray = new Office[officeCommands];
        String[] wantedArray = new String[wantedCommands];
        String[] commandsArray = new String[commandCommands];
        String[] commandTypesArray = new String[commandCommands];

        Pickup[] pickupArray = new Pickup[commandCommands];
        Letter[] letterArray = new Letter[commandCommands];
        Package[] packageArray = new Package[commandCommands];

            // Other variables
        int currentDay = 1;

        // FOR loop to read all offices into an array of post office objects, create log files and open writers
        for (int forPos = 0; forPos != officeCommands; forPos = forPos + 1) {
            officesArray[forPos] = new Office();
            officesArray[forPos].officeName = officeInput.next();
            officesArray[forPos].transitTime = officeInput.nextInt();
            officesArray[forPos].postage = officeInput.nextInt();
            officesArray[forPos].capacity = officeInput.nextInt();
            officesArray[forPos].persuasion = officeInput.nextInt();
            officesArray[forPos].maxPackageSize = officeInput.nextInt();
            officesArray[forPos].writer = new PrintWriter("log_" + officesArray[forPos].officeName + ".txt");
        }

        // Parse wanted criminals file into an array
        for (int forPos = 0; forPos != wantedCommands; forPos = forPos + 1) {
            wantedArray[forPos] = wantedInput.next();
        }

        // Parse each command into a relevant object, populate types array
        for (int forPos = 0; forPos != commandCommands; forPos = forPos + 1) {
            commandsArray[forPos] = commandInput.nextLine(); // Save an array of all the commands

            Scanner scanner = new Scanner(commandsArray[forPos]);
            commandTypesArray[forPos] = scanner.next(); // Populate types array

            if ("DAY".equals(commandTypesArray[forPos])) {}
            else if ("PICKUP".equals(commandTypesArray[forPos])) {
                pickupArray[forPos] = new Pickup();
                pickupArray[forPos].pickupOffice = scanner.next();
                pickupArray[forPos].person = scanner.next();
                pickupArray[forPos].status = 1;
            }
            else if ("LETTER".equals(commandTypesArray[forPos])) {
                letterArray[forPos] = new Letter();
                letterArray[forPos].originatingOffice = scanner.next();
                letterArray[forPos].recipient = scanner.next();
                letterArray[forPos].destinationOffice = scanner.next();
                letterArray[forPos].returnPickupName = scanner.next();
                letterArray[forPos].status = 1;
            }
            else if ("PACKAGE".equals(commandTypesArray[forPos])) {
                packageArray[forPos] = new Package();
                packageArray[forPos].originatingOffice = scanner.next();
                packageArray[forPos].recipient = scanner.next();
                packageArray[forPos].destinationOffice = scanner.next();
                packageArray[forPos].postage = scanner.nextInt();
                packageArray[forPos].packageLength = scanner.nextInt();
                packageArray[forPos].status = 1;
            }
            scanner.close();
        }

        // Parse commands file
            // FOR loop based on the number of commands
        for (int forPos = 0; forPos != commandCommands; forPos = forPos + 1) {
            // Check the type and do something based on that
                // DAY:
            if ("DAY".equals(commandTypesArray[forPos])) {
                // Run through all accepted items and change them to in transit
                for (int forPosTwo = 0; forPosTwo != commandCommands; forPosTwo = forPosTwo + 1) {
                    if ("LETTER".equals(commandTypesArray[forPosTwo]) && 2 == letterArray[forPosTwo].status) {
                        int tempOffice = OfficeID(letterArray[forPosTwo].originatingOffice, officesArray);
                        letterArray[forPosTwo].status = 3;
                        // update next action date
                        letterArray[forPosTwo].nextAction += officesArray[tempOffice].transitTime + 1;
                        officesArray[tempOffice].currentLoad -= 1; // update post office capacity
                        officesArray[tempOffice].writer.println("- Standard transit departure -"); // log stuff

                    } else if ("PACKAGE".equals(commandTypesArray[forPosTwo]) && 2 == packageArray[forPosTwo].status) {
                        packageArray[forPosTwo].status = 3;
                        int tempOffice = OfficeID(packageArray[forPosTwo].originatingOffice, officesArray);
                        // update next action date
                        packageArray[forPosTwo].nextAction += officesArray[tempOffice].transitTime + 1;
                        officesArray[tempOffice].currentLoad -= 1; // update post office capacity
                        officesArray[tempOffice].writer.println("- Standard transit departure -"); // log stuff
                    }
                }

                // Increment day
                masterWriter.println("- - DAY " + currentDay + " OVER - -");
                for (int forPosTwo = 0; forPosTwo != officeCommands; forPosTwo = forPosTwo + 1) {
                    officesArray[forPosTwo].writer.println("- - DAY " + currentDay + " OVER - -");
                }
                currentDay++;

                // Check for arrivals
                    // Run through all in transit items (status 3), add to post office
                for (int forPosTwo = 0; forPosTwo != commandCommands; forPosTwo = forPosTwo + 1) {
                    if ("LETTER".equals(commandTypesArray[forPosTwo])
                            &&
                            3 == letterArray[forPosTwo].status
                            &&
                            currentDay == letterArray[forPosTwo].nextAction) {
                        int tempOffice = OfficeID(letterArray[forPosTwo].destinationOffice, officesArray);

                        if (officesArray[tempOffice].currentLoad >= officesArray[tempOffice].capacity) {
                            letterArray[forPosTwo].status = 0; //set to inactive

                            masterWriter.println("- Incinerated letter -");
                            masterWriter.println("Destroyed at: " + letterArray[forPosTwo].destinationOffice);

                            officesArray[tempOffice].writer.println("- Incinerated letter -");
                            officesArray[tempOffice].writer.println("Destroyed at: " + letterArray[forPosTwo].destinationOffice);
                        } else {
                            letterArray[forPosTwo].status = 4;
                            letterArray[forPosTwo].nextAction += 14; // update next action date
                            officesArray[tempOffice].currentLoad += 1; // update post office capacity
                            officesArray[tempOffice].writer.println("- Standard transit arrival -"); // log stuff
                        }

                    } else if ("PACKAGE".equals(commandTypesArray[forPosTwo])
                            &&
                            3 == packageArray[forPosTwo].status
                            &&
                            currentDay == packageArray[forPosTwo].nextAction) {
                        int tempOffice = OfficeID(packageArray[forPosTwo].destinationOffice, officesArray);

                        if (officesArray[tempOffice].currentLoad >= officesArray[tempOffice].capacity
                            ||
                            officesArray[tempOffice].maxPackageSize < packageArray[forPosTwo].packageLength
                            ) {
                            packageArray[forPosTwo].status = 0; //set to inactive

                            masterWriter.println("- Incinerated package -");
                            masterWriter.println("Destroyed at: " + packageArray[forPosTwo].destinationOffice);

                            officesArray[tempOffice].writer.println("- Incinerated package -");
                            officesArray[tempOffice].writer.println("Destroyed at: " + packageArray[forPosTwo].destinationOffice);
                        } else {
                            packageArray[forPosTwo].status = 4;
                            packageArray[forPosTwo].nextAction += 14; // update next action date
                            officesArray[tempOffice].currentLoad += 1; // update post office capacity
                            officesArray[tempOffice].writer.println("- Standard transit arrival -"); // log stuff
                        }
                    }
                }

                // Check waiting items that have been there too long
                    // Run through all waiting items (status 4), check if 14 days
                for (int forPosTwo = 0; forPosTwo != commandCommands; forPosTwo = forPosTwo + 1) {
                    if ("LETTER".equals(commandTypesArray[forPosTwo])
                            &&
                            4 == letterArray[forPosTwo].status
                            &&
                            currentDay == letterArray[forPosTwo].nextAction) {
                        int tempOffice = OfficeID(letterArray[forPosTwo].destinationOffice, officesArray);

                        if (letterArray[forPosTwo].returnPickupName.equals("NONE")) {
                            letterArray[forPosTwo].status = 0; //set to inactive

                            masterWriter.println("- Incinerated letter -");
                            masterWriter.println("Destroyed at: " + letterArray[forPosTwo].destinationOffice);

                            officesArray[tempOffice].writer.println("- Incinerated letter -");
                            officesArray[tempOffice].writer.println("Destroyed at: " + letterArray[forPosTwo].destinationOffice);
                        } else {
                            letterArray[forPosTwo].recipient = letterArray[forPosTwo].returnPickupName;
                            letterArray[forPosTwo].returnPickupName = "NONE";

                            String newDestination = letterArray[forPosTwo].originatingOffice;
                            String newOrigination = letterArray[forPosTwo].destinationOffice;

                            letterArray[forPosTwo].destinationOffice = newDestination;
                            letterArray[forPosTwo].originatingOffice = newOrigination;

                            letterArray[forPosTwo].status = 2;
                            letterArray[forPosTwo].nextAction += officesArray[tempOffice].transitTime; // update next action date
                            officesArray[tempOffice].currentLoad -= 1; // update post office capacity

                            officesArray[tempOffice].writer.println("- New letter -");
                            officesArray[tempOffice].writer.println("Source: " + newOrigination);
                            officesArray[tempOffice].writer.println("Destination: " + newDestination);
                            officesArray[tempOffice].writer.println("- Accepted letter -");
                            officesArray[tempOffice].writer.println("Destination: " + newDestination);
                        }

                    } else if ("PACKAGE".equals(commandTypesArray[forPosTwo])
                            &&
                            4 == packageArray[forPosTwo].status
                            &&
                            currentDay == packageArray[forPosTwo].nextAction) {
                        int tempOffice = OfficeID(packageArray[forPosTwo].destinationOffice, officesArray);
                        packageArray[forPosTwo].status = 0; //set to inactive

                        masterWriter.println("- Incinerated package -");
                        masterWriter.println("Destroyed at: " + packageArray[forPosTwo].destinationOffice);

                        officesArray[tempOffice].writer.println("- Incinerated package -");
                        officesArray[tempOffice].writer.println("Destroyed at: " + packageArray[forPosTwo].destinationOffice);
                    }
                }
            }

                // PICKUP:
            else if ("PICKUP".equals(commandTypesArray[forPos])) {
                // Check if they're a criminal, if so log it
                if (CriminalCheck(pickupArray[forPos].person, wantedArray)) {
                    frontWriter.println("- AREA MANS SHARES FIRST NAME WITH CRIMINAL -");
                }
                else {
                    String pickupPerson = pickupArray[forPos].person;
                    String pickupOffice = pickupArray[forPos].pickupOffice;
                    int tempOffice = OfficeID(pickupOffice, officesArray);

                    int letterPickupDays = -1;
                    int packagePickupDays = -1;
                    for (int forPosTwo = 0; forPosTwo != commandCommands; forPosTwo = forPosTwo + 1) {
                        if ("LETTER".equals(commandTypesArray[forPosTwo])) {
                            if (pickupPerson.equals(letterArray[forPosTwo].recipient)
                                    &&
                                    pickupOffice.equals(letterArray[forPosTwo].destinationOffice)
                                    &&
                                    4 == letterArray[forPosTwo].status) {
                                letterPickupDays = currentDay - letterArray[forPosTwo].enteredSystem + 1;
                            }
                            if (letterPickupDays >= 0) {
                                officesArray[tempOffice].writer.println("- Delivery process complete -");
                                officesArray[tempOffice].writer.println("Delivery took " + letterPickupDays + " days.");
                                pickupArray[forPos].status = 0;
                                letterArray[forPosTwo].status = 0;
                                letterPickupDays = -1;
                            }
                        }
                        else if ("PACKAGE".equals(commandTypesArray[forPosTwo])) {
                            if (pickupPerson.equals(packageArray[forPosTwo].recipient)
                                    &&
                                    pickupOffice.equals(packageArray[forPosTwo].destinationOffice)
                                    &&
                                    4 == packageArray[forPosTwo].status) {
                                packagePickupDays = currentDay - packageArray[forPosTwo].enteredSystem + 1;
                            }
                            if (packagePickupDays >= 0) {
                                officesArray[tempOffice].writer.println("- Delivery process complete -");
                                officesArray[tempOffice].writer.println("Delivery took " + packagePickupDays + " days.");
                                pickupArray[forPos].status = 0;
                                packageArray[forPosTwo].status = 0;
                                packagePickupDays = -1;
                            }
                        }
                    }
                }
            }

                // LETTER:
            else if ("LETTER".equals(commandTypesArray[forPos])) {
                // What office is this
                int tempOffice = OfficeID(letterArray[forPos].originatingOffice, officesArray);

                // Handle preliminary drop off activities
                if (-1 == tempOffice) {} // If this is brought to a non-existent post office
                else {
                    officesArray[tempOffice].writer.println("- New letter -");
                    officesArray[tempOffice].writer.println("Source: " + letterArray[forPos].originatingOffice);
                    officesArray[tempOffice].writer.println("Destination: " + letterArray[forPos].destinationOffice);

                    // Check reject status
                    if (    -1 == OfficeID(letterArray[forPos].destinationOffice, officesArray)
                            ||
                            CriminalCheck(letterArray[forPos].recipient, wantedArray)
                            ||
                            officesArray[tempOffice].capacity <= officesArray[tempOffice].currentLoad
                            ) {
                        officesArray[tempOffice].writer.println("- Rejected letter -");
                        officesArray[tempOffice].writer.println("Source: " + letterArray[forPos].originatingOffice);
                        masterWriter.println("- Rejected letter -");
                        masterWriter.println("Source: " + letterArray[forPos].originatingOffice);

                        letterArray[forPos].status = 0;
                    }
                    else {
                        // If not rejected, accept the item
                        officesArray[tempOffice].writer.println("- Accepted letter -");
                        officesArray[tempOffice].writer.println("Destination: " + letterArray[forPos].destinationOffice);

                        // Update letter status and next action date
                        letterArray[forPos].status = 2;
                        officesArray[tempOffice].currentLoad += 1;
                        letterArray[forPos].enteredSystem = currentDay;
                        letterArray[forPos].nextAction = currentDay;
                    }
                }
            }

                // PACKAGE:
            else if ("PACKAGE".equals(commandTypesArray[forPos])) {
                // What office is this
                int tempOffice = OfficeID(packageArray[forPos].originatingOffice, officesArray);
                int tempDesOffice = OfficeID(packageArray[forPos].destinationOffice, officesArray);

                // Handle preliminary drop off activities
                if (-1 == tempOffice) {} // If this is brought to a non-existent post office
                else {
                    officesArray[tempOffice].writer.println("- New package -");
                    officesArray[tempOffice].writer.println("Source: " + packageArray[forPos].originatingOffice);
                    officesArray[tempOffice].writer.println("Destination: " + packageArray[forPos].destinationOffice);

                    // Check reject status
                    if (    -1 == OfficeID(packageArray[forPos].destinationOffice, officesArray)
                            ||
                            CriminalCheck(packageArray[forPos].recipient, wantedArray)
                            ||
                            officesArray[tempOffice].capacity <= officesArray[tempOffice].currentLoad
                            ||
                            packageArray[forPos].postage <= officesArray[tempOffice].postage
                            ||
                            (
                                    (
                                    packageArray[forPos].packageLength >= officesArray[tempOffice].maxPackageSize
                                    ||
                                    packageArray[forPos].packageLength >= officesArray[tempDesOffice].maxPackageSize
                                    )
                            &&
                            packageArray[forPos].postage < officesArray[tempOffice].postage + officesArray[tempOffice].persuasion
                            )
                            ) {
                        officesArray[tempOffice].writer.println("- Rejected package -");
                        officesArray[tempOffice].writer.println("Source: " + packageArray[forPos].originatingOffice);
                        masterWriter.println("- Rejected package -");
                        masterWriter.println("Source: " + packageArray[forPos].originatingOffice);
                        packageArray[forPos].status = 0;
                    }
                    else if (
                            (packageArray[forPos].packageLength >= officesArray[tempOffice].maxPackageSize
                            ||
                            packageArray[forPos].packageLength >= officesArray[tempDesOffice].maxPackageSize)
                            &&
                            packageArray[forPos].postage >= officesArray[tempOffice].postage + officesArray[tempOffice].persuasion
                            ) {
                        // Bribery, accept the item
                        officesArray[tempOffice].writer.println("- Accepted package -");
                        officesArray[tempOffice].writer.println("Destination: " + packageArray[forPos].destinationOffice);

                        masterWriter.println("- Something funny going on... -");
                        masterWriter.println("Where did that extra money at " + officesArray[tempOffice].officeName + " come from?");

                        // Update package status and next action date
                        packageArray[forPos].status = 2;
                        officesArray[tempOffice].currentLoad += 1;
                        packageArray[forPos].enteredSystem = currentDay;
                        packageArray[forPos].nextAction = currentDay;
                    }
                    else {
                        // If not rejected, accept the item
                        officesArray[tempOffice].writer.println("- Accepted package -");
                        officesArray[tempOffice].writer.println("Destination: " + packageArray[forPos].destinationOffice);

                        // Update package status and next action date
                        packageArray[forPos].status = 2;
                        officesArray[tempOffice].currentLoad += 1;
                        packageArray[forPos].enteredSystem = currentDay;
                        packageArray[forPos].nextAction = currentDay;
                    }
                }
            }
        }

        // Clean up
            // Close writers
        for (int forPos = 0; forPos != officeCommands; forPos = forPos + 1) {
            officesArray[forPos].writer.close();
        }
        masterWriter.close();
        frontWriter.close();

            // Close scanners
        officeInput.close();
        wantedInput.close();
        commandInput.close();
    }
}