class Loops {
    public static void main(String[] args) {
        //Creating array;
        int n= 100;
        int xArray[]= new int[n];
        //defining I as an Integer
        int i;
        // for loop to cycle through xArray
        for(i = 0; i <= n; i++){
            // if statement to check if number is even or odd and if even print out number
            if (i % 2 == 0){
                System.out.print(i + " ");
            }
        }

    }
}