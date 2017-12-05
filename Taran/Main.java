import java.io.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        create_master_and_front_log();
        try {
            create_log_for_post_offices();
            read_commands_file();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class PostOffice {
        public String name;
        public int transitTime;
        public int postageCost;
        public int storageCapacity;
        public int dollarPersuasion;
        public int maximumPackageSize;
        public MailItem[] mailItems;
        public MailItem[] transitMail = new MailItem[100];

        int itemCount = 0;
        static int dayCount = 1;

        public PostOffice(){}

        public PostOffice(String name, int transitTime, int postageCost, int storageCapacity, int dollarPersuasion, int maximumPackageSize) {
            this.name = name;
            this.transitTime = transitTime;
            this.postageCost = postageCost;
            this.storageCapacity = storageCapacity;
            this.dollarPersuasion = dollarPersuasion;
            this.maximumPackageSize = maximumPackageSize;
            this.mailItems = new MailItem[storageCapacity];
        }


        public static void increment_day_and_log_end_of_day() {
            dayCount++;
        }

        //this function is meant to shift the array items from the storage to a temporary transit array and logs to post office
        //a log message for each mail item, this movement happens at the end of the day.
        public void move_storage_mail_to_transit_and_log_at_end_of_day() {
            for (int i = 0; i < itemCount; i++) {

                if(mailItems[i] != null){
                    MailItem aMailItem = this.mailItems[i];
                    if(!aMailItem.destinationPostOffice.equals(this.name)){
                        aMailItem.dayAccepted = dayCount;
                        this.transitMail[i] = aMailItem;
                        this.mailItems[i] = null;
                        try {
                            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter("log_" + name + ".txt", true)));
                            printWriter.write("- Standard transit departure -\n");
                            printWriter.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
            this.itemCount = 0;
        }

        //this happens at the beginning of each day.
        public void send_transit_to_receiving(PostOffice [] postOffices, Criminal [] criminals) throws IOException {
            for (int i = 0; i != transitMail.length; i++) {
                //we need to send this mail item to its destination. We need to grab a reference to that office.
                MailItem mailItem = transitMail[i];

                if(mailItem != null && !mailItem.destinationPostOffice.equals(this.name)){
                    if ((dayCount == (mailItem.dayAccepted+this.transitTime))) {
                        PostOffice destinationOffice = grab_a_reference_to_post_office_based_on_name(postOffices, mailItem.destinationPostOffice);
                        destinationOffice.receive_transit_mail(mailItem);
                    }
                }

            }
        }

        public void receive_transit_mail(MailItem mailItem){

            if(this.itemCount == this.storageCapacity){
                this.destroy_mail(mailItem);
            }
            else if(mailItem instanceof Package){
                Package mPackage = (Package) mailItem;

                if(mPackage.lengthOfThePackage > this.maximumPackageSize){
                    message_to_add_to_file("log_" + this.name + ".txt", "- Standard transit arrival -");
                    this. destroy_mail(mPackage);
                }else {
                    if(this.mailItems[itemCount] == null){
                        if(mailItem != null){
                            this.store_mail(mailItem);
                            message_to_add_to_file("log_" + this.name + ".txt", "- Standard transit arrival -");
                        }
                    }
                }
            }else {

                if(this.mailItems[itemCount] == null){
                        if(mailItem != null){
                            this.store_mail(mailItem);
                            message_to_add_to_file("log_" + this.name + ".txt", "- Standard transit arrival -");
                        }
                    }
            }
        }

        public void destroy_the_mail_if_it_has_been_too_long(){
            //call destory if dayaccpeted + 14 is equal to daycount.
            for(MailItem mailItem : mailItems){
                if(mailItem instanceof Letter){
                    Letter mLetter = (Letter) mailItem;
                    if(mLetter != null && dayCount == (mLetter.dayAccepted+14) && mLetter.destinationPostOffice.equals(this.name) && mLetter.personToReturnTo.equals("NONE")){
                        this.destroy_mail(mLetter);
                    }
                } else if(mailItem instanceof Package){
                    Package mPackage = (Package) mailItem;
                    if(mPackage != null && dayCount == (mPackage.dayAccepted+14) && mPackage.destinationPostOffice.equals(this.name)){
                        this.destroy_mail(mPackage);
                    }
                }

            }
        }

        public void destroy_mail(MailItem mailItem){
            if (mailItem instanceof Package) {
                try {
                    PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter("log_master.txt", true)));
                    printWriter.write("- Incinerated package -\n");
                    printWriter.write("Destroyed at: " + this.name + "\n");
                    printWriter.close();

                    printWriter = new PrintWriter(new BufferedWriter(new FileWriter("log_" + name + ".txt", true)));
                    printWriter.write("- Incinerated package -\n");
                    printWriter.write("Destroyed at: " + this.name + "\n");
                    printWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (mailItem instanceof Letter) {
                try {
                    PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter("log_master.txt", true)));
                    printWriter.write("- Incinerated letter -\n");
                    printWriter.write("Destroyed at: " + this.name + "\n");
                    printWriter.close();

                    printWriter = new PrintWriter(new BufferedWriter(new FileWriter("log_" + this.name + ".txt", true)));
                    printWriter.write("- Incinerated letter -\n");
                    printWriter.write("Destroyed at: " + this.name + "\n");
                    printWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void resend_letter_if_it_has_been_too_long(PostOffice [] postOffices, Criminal [] criminals) throws IOException {
            for(MailItem mailItem : mailItems){
                if(mailItem instanceof Letter){
                    Letter mLetter = (Letter) mailItem;
                    if(mLetter != null && dayCount == (mLetter.dayAccepted+14) && mLetter.destinationPostOffice.equals(this.name) && !mLetter.personToReturnTo.equals("NONE")){
                        String newInitial = mLetter.destinationPostOffice;
                        String newDestination = mLetter.initialPostOffice;
                        String newRecipient = mLetter.personToReturnTo;
                        String newReturn = "NONE";

                        Letter anotherLetter = new Letter(newInitial,newRecipient,newDestination,newReturn);

                        this.receive_mail(anotherLetter,postOffices,criminals);
                        this.send_transit_to_receiving(postOffices,criminals);
                    }
                }
            }
        }


        public void receive_mail(MailItem mail, PostOffice [] postOffices, Criminal [] criminals) {
            try {
                PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter("log_" + name + ".txt", true)));

                PostOffice destination = grab_a_reference_to_post_office_based_on_name(postOffices, mail.destinationPostOffice);
                if (mail instanceof Package) {
                    Package mPackage = (Package) mail;

                    printWriter.write("- New package -\n");
                    printWriter.write("Source: " + mail.initialPostOffice + "\n");
                    printWriter.write("Destination: " + mail.destinationPostOffice + "\n");
                    printWriter.close();

                    if(false == scan_array_postoffices_style_for_name_presence(postOffices, mPackage.destinationPostOffice)
                            || this.itemCount == storageCapacity
                            || scan_array_criminals_style_for_name(criminals, mPackage.recipient)){
                        this.reject_mail(mPackage);
                    }
                    else if((mPackage.moneyWithPackage >= (this.postageCost + this.dollarPersuasion))
                            && this.itemCount < storageCapacity)
                    {//this will be for the bribery time where the persuasion amount is good regardless of size. NEED TO STORE THE ITEM TOO.
                        this.accept_mail(mPackage,destination);
                        message_to_add_to_file("log_master.txt", "- Something funny going on... -");
                        message_to_add_to_file("log_master.txt", "Where did that extra money at "+this.name+" come from?");
                    }

                    else if(mPackage.moneyWithPackage < this.postageCost
                            || (mPackage.moneyWithPackage >= this.postageCost && mPackage.lengthOfThePackage > this.maximumPackageSize)
                            || (mPackage.moneyWithPackage >= this.postageCost && mPackage.lengthOfThePackage > destination.maximumPackageSize)){
                        this.reject_mail(mPackage);
                    }else {
                        this.accept_mail(mPackage, destination);
                    }


                } else if (mail instanceof Letter) {
                    Letter mLetter = (Letter) mail;
                    printWriter.write("- New letter -\n");
                    printWriter.write("Source: " + mail.initialPostOffice + "\n");
                    printWriter.write("Destination: " + mail.destinationPostOffice + "\n");
                    printWriter.close();

                    if(false == scan_array_postoffices_style_for_name_presence(postOffices, mLetter.destinationPostOffice)
                            || scan_array_criminals_style_for_name(criminals, mLetter.recipient)
                            || this.itemCount == storageCapacity){
                        this.reject_mail(mLetter);
                    }else {
                        this.accept_mail(mLetter, destination);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void reject_mail(MailItem mailItem) {
            if (mailItem instanceof Package) {
                try {
                    PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter("log_master.txt", true)));
                    printWriter.write("- Rejected package -\n");
                    printWriter.write("Source: " + this.name + "\n");
                    printWriter.close();

                    printWriter = new PrintWriter(new BufferedWriter(new FileWriter("log_" + name + ".txt", true)));
                    printWriter.write("- Rejected package -\n");
                    printWriter.write("Source: " + this.name + "\n");
                    printWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (mailItem instanceof Letter) {
                try {
                    PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter("log_master.txt", true)));
                    printWriter.write("- Rejected letter -\n");
                    printWriter.write("Source: " + this.name + "\n");
                    printWriter.close();

                    printWriter = new PrintWriter(new BufferedWriter(new FileWriter("log_" + this.name + ".txt", true)));
                    printWriter.write("- Rejected letter -\n");
                    printWriter.write("Source: " + this.name + "\n");
                    printWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void accept_mail(MailItem mailItem, PostOffice postOffice){
            if (mailItem instanceof Package) {
                try {

                    PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter("log_" + name + ".txt", true)));
                    printWriter.write("- Accepted package -\n");
                    printWriter.write("Destination: " + mailItem.destinationPostOffice + "\n");
                    printWriter.close();
                    this.store_mail(mailItem);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (mailItem instanceof Letter) {
                try {

                    PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter("log_" + name + ".txt", true)));
                    printWriter.write("- Accepted letter -\n");
                    printWriter.write("Destination: " + mailItem.destinationPostOffice + "\n");
                    printWriter.close();
                    this.store_mail(mailItem);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        public boolean store_mail(MailItem mailItem){
            boolean accepted = false;

            if(this.itemCount < this.storageCapacity){
                this.mailItems[itemCount] = mailItem;
                this.itemCount++;
                accepted = true;
                return accepted;
            }
            return accepted;
        }
    }


    public static class MailItem {
        public String initialPostOffice;
        public String destinationPostOffice;
        public String recipient;
        public int dayAccepted;

        public MailItem(){

        }
    }

    public static class Package extends MailItem {
        public int moneyWithPackage;
        public int lengthOfThePackage;

        public Package(){

        }

        public Package(String initialPostOffice, String destinationPostOffice, String recipient, int dayAccepted, int moneyWithPackage, int lengthOfThePackage){
            this.initialPostOffice = initialPostOffice;
            this.destinationPostOffice = destinationPostOffice;
            this.recipient = recipient;
            this.dayAccepted = dayAccepted;
            this.moneyWithPackage = moneyWithPackage;
            this.lengthOfThePackage = lengthOfThePackage;
        }
    }

    public static class Letter extends MailItem {
        public String personToReturnTo;

        public Letter(String initialPostOffice, String recipient, String destinationPostOffice, String personToReturnTo){
            this.initialPostOffice = initialPostOffice;
            this.recipient = recipient;
            this.destinationPostOffice = destinationPostOffice;
            this.personToReturnTo = personToReturnTo;
        }
    }

    public static class Criminal {
        public String name;
        public Criminal(String name){
            this.name = name;
        }
    }

    public static void create_master_and_front_log() {

        try {
            PrintWriter printWriter = new PrintWriter("log_master.txt");
            printWriter.close();
            printWriter = new PrintWriter("log_front.txt");
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void create_log_for_post_offices() throws IOException {
        PostOffice[] postOffices = create_postoffice_from_file("offices.txt");
        if(postOffices != null){
            for (PostOffice postOffice : postOffices) {
                PrintWriter printWriter = new PrintWriter("log_" + postOffice.name + ".txt");
                printWriter.close();

            }
        }

    }

    public static void message_to_add_to_file(String logFile, String message){
        try {
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
            printWriter.write(message+"\n");
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PostOffice[] create_postoffice_from_file(String fileName) throws IOException {

        Scanner inputFile = new Scanner(new File(fileName));

        int myInt = inputFile.nextInt();
        if (myInt > 0) {
            PostOffice[] postOffices = new PostOffice[myInt];
            int counter = 0;

            while (inputFile.hasNext()) {
                PostOffice postOffice = new PostOffice(inputFile.next(), inputFile.nextInt(), inputFile.nextInt(), inputFile.nextInt(), inputFile.nextInt(), inputFile.nextInt());
                postOffices[counter] = postOffice;
                counter++;
            }
            inputFile.close();
            return postOffices;
        } else if (myInt == 0) {
            return null;
        }

        throw new IOException();
    }


    public static PostOffice grab_a_reference_to_post_office_based_on_name(PostOffice[] array, String name) {

        PostOffice postOffice = new PostOffice();
        if (scan_array_postoffices_style_for_name_presence(array, name)) {

            for (int i = 0; i != array.length; i++) {
                if (array[i].name.equals(name)) {
                    postOffice = array[i];
                }
            }
            return postOffice;
        }
        return postOffice;
    }

    public static boolean scan_array_postoffices_style_for_name_presence(PostOffice[] array, String needle) {

        boolean found = false;
        for (PostOffice arrayItem : array) {
            if (arrayItem.name.equals(needle)) {
                found = true;
            }
        }
        return found;
    }

    public static boolean scan_array_criminals_style_for_name(Criminal [] array, String needle){

        boolean found = false;
        for(Criminal arrayItem : array){
            if(arrayItem.name.equals(needle)) {
                found = true;
            }
        }
        return found;
    }

    public static Criminal [] create_criminal_from_file(String fileName) throws FileNotFoundException {

        Scanner inputFile = new Scanner(new File(fileName));

        int myInt = inputFile.nextInt();
        Criminal [] criminals = new Criminal[myInt];

        if(myInt > 0){

            int counter = 0;
            while(inputFile.hasNext()){
                Criminal criminal = new Criminal(inputFile.next());
                criminals[counter] = criminal;
                counter++;
            }
            inputFile.close();

            return criminals;
        }else if(myInt <= 0) {
            return criminals;
        }

        throw new FileNotFoundException();
    }

    public static void read_commands_file() throws IOException {
        try {
            Scanner commandInput = new Scanner(new File("commands.txt"));
            String [] commands = new String [commandInput.nextInt()];

            PostOffice [] postOffices = create_postoffice_from_file("offices.txt");
            Criminal [] criminals = create_criminal_from_file("wanted.txt");

            int counter = 0;
            while(counter < commands.length){
                String command = commandInput.next();

                switch (command){
                    case "DAY":

                        message_to_add_to_file("log_master.txt", "- - DAY " + PostOffice.dayCount + " OVER - -");
                        for(PostOffice postOffice : postOffices){
                            postOffice.move_storage_mail_to_transit_and_log_at_end_of_day();
                            message_to_add_to_file("log_"+postOffice.name+".txt", "- - DAY " + PostOffice.dayCount + " OVER - -");

                        }
                        for(PostOffice postOffice : postOffices){
                            //postOffice.move_storage_mail_to_transit_and_log_at_end_of_day();
                            postOffice.send_transit_to_receiving(postOffices,criminals);
                            postOffice.destroy_the_mail_if_it_has_been_too_long();
                            //postOffice.resend_letter_if_it_has_been_too_long(postOffices,criminals);
                        }

                        for(PostOffice postOffice : postOffices){
                            postOffice.resend_letter_if_it_has_been_too_long(postOffices,criminals);
                        }

                        PostOffice.increment_day_and_log_end_of_day();

                        break;

                    case "PACKAGE":

                        String postOfficeName = commandInput.next();
                        String recipient = commandInput.next();
                        String destination = commandInput.next();
                        int moneyWithPackage = commandInput.nextInt();
                        int lengthOfPackage = commandInput.nextInt();

                        PostOffice postOffice = grab_a_reference_to_post_office_based_on_name(postOffices, postOfficeName);
                        Package mPackage = new Package(postOfficeName,destination,recipient,PostOffice.dayCount,moneyWithPackage,lengthOfPackage);

                        postOffice.receive_mail(mPackage,postOffices,criminals);
                        break;

                    case "LETTER":

                        postOfficeName = commandInput.next();
                        recipient = commandInput.next();
                        destination = commandInput.next();
                        String sender = commandInput.next();

                        Letter mLetter = new Letter(postOfficeName,recipient,destination,sender);
                        postOffice = grab_a_reference_to_post_office_based_on_name(postOffices,postOfficeName);

                        postOffice.receive_mail(mLetter,postOffices,criminals);
                        break;

                    case "PICKUP":

                        postOfficeName = commandInput.next();
                        recipient = commandInput.next();

                        check_to_apprehend_the_criminal(recipient, criminals);

                        postOffice = grab_a_reference_to_post_office_based_on_name(postOffices, postOfficeName);
                        //cycle through the postoffice and then check to see if there is mail for that recipient in storage
                        for(int i = 0; i != postOffice.itemCount; i++){
                            if(postOffice.mailItems[i].destinationPostOffice.equals(postOfficeName) && postOffice.mailItems[i].recipient.equals(recipient)){
                                message_to_add_to_file("log_"+postOfficeName+".txt", "- Delivery process complete -");
                                message_to_add_to_file("log_"+postOfficeName+".txt", "Delivery took " +(PostOffice.dayCount-1)+" days.");
                                postOffice.mailItems[i] = null;
                            }
                        }
                        break;

                    default:
                        System.out.println("Error");
                }
                counter++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void check_to_apprehend_the_criminal(String pickerUpper, Criminal [] criminals){
        if(true == scan_array_criminals_style_for_name(criminals, pickerUpper)){
            message_to_add_to_file("log_front.txt", "We caught that sucka... we caught a criminal");
        }
    }
}

