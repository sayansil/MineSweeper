/**
* CLI Minesweeper game
* @author Sayan Sil (Principal), Tamoghna Chowdhury (Editor)
* @version 1.1
* Organization St. James' School
*/

import java.util.Scanner;
public class MineSweeper extends DeveloperMS
{
    static int n,mine;// n->dimension of grid, mine-> number of mines
    static String M[][]; // Actual randomly generated grid
    static String Md[][]; // Grid displayed to the player

    public static void main(String[] args){
        begin();
    }
    
    protected static void begin()
    {
        Scanner sc=new Scanner(System.in);
        MineSweeper game=new MineSweeper();
        game.startPage();
    }

    public void startPage()
    {
        Scanner sc=new Scanner(System.in);
        
        n=super.n1;
        decide();
        assign();
        mine=super.mine1;
        
        randomlyStart();
        
        long start_time=System.nanoTime();
        rightHere();
        long end_time=System.nanoTime();
        double time=(int)((end_time-start_time)/1E9);
        System.out.print("\n\nTIME TAKEN: ");
        if((int)(time/60)>0)
        {
            System.out.print((int)(time/60));
            if((int)(time/60)==1)
                System.out.print(" minute ");
            else
                System.out.print(" minutes ");
        }
        System.out.print((int)(time%60));
        if((int)(time%60)==1)
            System.out.print(" second");
        else
            System.out.print(" seconds");
    }

    public void randomlyStart()// flips a few random tiles at the beginning of the game
    {
        int i,j;

        for(int x=0;x<mine+2;x++)
        {
            do
            {
                i=(int)(Math.random()*n);
                j=(int)(Math.random()*n);
            }while(M[i][j]=="X"||Md[i][j]!="*");
            Md[i][j]=M[i][j];
        }

    }

    public void rightHere()// turn based game starts here
    {
        while(zeroes())//if there exists any null/zero boxes
        {
            for(int i=0;i<n;i++)
                for(int j=0;j<n;j++)
                    if(Md[i][j].equals("0"))
                    {
                        publishAllSides(i,j);//flips all boxes around a null box
                    }
        }

        Scanner sc=new Scanner(System.in);
        System.out.println("\u000c");
        System.out.println(); 

        System.out.println("\nNo. of mines: "+mine+"\n\n");
        display(Md);
        System.out.println("\n\n          Enter co-ordinates:-");//accepts the co-ordinates to flip that box
        System.out.print("          ");
        int i=sc.nextInt();
        System.out.print("          ");
        int j=sc.nextInt();

        if(M[i][j]=="X")//if the box is a mine
        {
            GameOver();
            return;
        }

        Md[i][j]=M[i][j];

        if(checkWin()==mine)//if all safe boxes are flipped
        {
            GameWon();
            return;
        }

        rightHere();//recursive nature for the next turn
    }

    public boolean zeroes()//returns true if there is a null boxin Md[][]
    {
        for(int i=0;i<n;i++)
            for(int j=0;j<n;j++)
                if(Md[i][j].equals("0"))
                    return true;
        return false;
    }

    public void publishAllSides(int i,int j)//flips all boxes around the box of supplied index
    {
        if(i-1>=0)
        {
            Md[i-1][j]=M[i-1][j];
            if(j-1>=0)
                Md[i-1][j-1]=M[i-1][j-1];
            if(j+1<n)
                Md[i-1][j+1]=M[i-1][j+1];
        }
        if(i+1<n)
        {
            Md[i+1][j]=M[i+1][j];
            if(j-1>=0)
                Md[i+1][j-1]=M[i+1][j-1];
            if(j+1<n)
                Md[i+1][j+1]=M[i+1][j+1];
        }
        if(j-1>=0)
            Md[i][j-1]=M[i][j-1];
        if(j+1<n)
            Md[i][j+1]=M[i][j+1];
        Md[i][j]=M[i][j]=" ";
    }

    public int checkWin()//if all safe boxes are flipped
    {
        int count=0;
        for(int i=0;i<n;i++)
            for(int j=0;j<n;j++)
                if(Md[i][j]=="*")
                    count++;
        return count;
    }

    public void display(String arr[][])//displays the matrix in required format
    {
        System.out.print("                    ");        
        for(int i=0;i<n;i++)
        {
            if((i+"").length()==1)
                System.out.print(" ");
            System.out.print(i+"  ");
        }
        System.out.println("\n");
        for(int i=0;i<n;i++)
        {
            System.out.print("                 "+i);
            if((i+"").length()==1)
                System.out.print(" ");
            for(int j=0;j<n;j++)
            {
                System.out.print(" "+arr[i][j]+" ");
                if(j!=(n-1))
                {
                    System.out.print("|");
                }
            }

            System.out.println();
            System.out.print("                    ");
            if(i!=(n-1))
            {
                for(int x=0;x<3+4*(n-1);x++)
                    System.out.print("-");
                System.out.println();
            }
        }

    }

    public void GameOver()//when oe loses a gmae
    {
        System.out.println("\u000c");
        System.out.println("OOOooPSS!!! u stepped on a mine!");
        System.out.println();        
        display(M);
        System.out.println("\n\n    *******************************");
        System.out.println("\n               GAME OVER!");
        System.out.println("\n    *******************************");
    }

    public void GameWon()//when one wins a game
    {
        System.out.println("\u000c");
        System.out.println("HURRRRAAAAAYYYYYYYYY!!!! :D :D :D :D :D");
        System.out.println("You have won the game!!!!!!!!!!");
        System.out.println();        
        display(M);

        System.out.println("\n\n    *******************************");
        System.out.println("\n               well played!");
        System.out.println("                *applause*");
        System.out.println("\n    *******************************");
    }

    public int check1(int i,int j)//counts the number of mines around a box
    {
        int count=0;
        if(i==0&&j==0)
        {
            if(M[0][1].equals("X"))
                count++;
            if(M[1][1].equals("X"))
                count++;
            if(M[1][0].equals("X"))
                count++;
            return count;
        }

        if(i==0&&j==n-1)
        {
            if(M[0][n-2].equals("X"))
                count++;
            if(M[1][n-2].equals("X"))
                count++;
            if(M[1][n-1].equals("X"))
                count++;
            return count;
        }

        if(i==n-1&&j==0)
        {
            if(M[n-1][1].equals("X"))
                count++;
            if(M[n-2][1].equals("X"))
                count++;
            if(M[n-1][0].equals("X"))
                count++;
            return count;
        }

        if(i==n-1&&j==n-1)
        {
            if(M[n-1][n-2].equals("X"))
                count++;
            if(M[n-2][n-2].equals("X"))
                count++;
            if(M[n-2][n-1].equals("X"))
                count++;
            return count;
        }

        if(i==0)
        {
            if(M[1][j].equals("X"))
                count++;
            if(M[0][j-1].equals("X"))
                count++;
            if(M[0][j+1].equals("X"))
                count++;
            if(M[1][j+1].equals("X"))
                count++;
            if(M[1][j-1].equals("X"))
                count++;
            return count;
        }

        if(i==n-1)
        {
            if(M[n-2][j].equals("X"))
                count++;
            if(M[n-1][j-1].equals("X"))
                count++;
            if(M[n-1][j+1].equals("X"))
                count++;
            if(M[n-2][j+1].equals("X"))
                count++;
            if(M[n-2][j-1].equals("X"))
                count++;
            return count;
        }

        if(j==0)
        {
            if(M[i][1].equals("X"))
                count++;
            if(M[i-1][0].equals("X"))
                count++;
            if(M[i+1][0].equals("X"))
                count++;
            if(M[i+1][1].equals("X"))
                count++;
            if(M[i-1][1].equals("X"))
                count++;
            return count;
        }

        if(j==n-1)
        {
            if(M[i][n-2].equals("X"))
                count++;
            if(M[i-1][n-1].equals("X"))
                count++;
            if(M[i+1][n-1].equals("X"))
                count++;
            if(M[i+1][n-2].equals("X"))
                count++;
            if(M[i-1][n-2].equals("X"))
                count++;
            return count;
        }

        if(M[i][j+1].equals("X"))
            count++;
        if(M[i][j-1].equals("X"))
            count++;
        if(M[i+1][j].equals("X"))
            count++;
        if(M[i-1][j].equals("X"))
            count++;
        if(M[i-1][j-1].equals("X"))
            count++;
        if(M[i-1][j+1].equals("X"))
            count++;
        if(M[i+1][j-1].equals("X"))
            count++;
        if(M[i+1][j+1].equals("X"))
            count++;
        return count;
    }

    public void assign()//assigns the numbers to safe boxes
    {
        for(int i=0;i<n;i++)
            for(int j=0;j<n;j++)
                if(!M[i][j].equals("X"))
                    M[i][j]=check1(i,j)+"";
    }

    public void decide()//creates the grid and randomly inserts the mines
    {
        M=new String[n][n];
        Md=new String[n][n];

        for(int i=0;i<n;i++)
            for(int j=0;j<n;j++)
                Md[i][j]="*";

        for(int i=0;i<n;i++)
            for(int j=0;j<n;j++)
                M[i][j]="0";

        int i,j;
        while(mine!=0)
        {
            do
            {
                i=(int)(Math.random()*n);
                j=(int)(Math.random()*n);
            }while(M[i][j].equals("X"));
            M[i][j]="X";
            mine--;
        }
    }
}
