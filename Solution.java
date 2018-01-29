
import utilClasses.Event;
import utilClasses.Status;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter path to logFolder: ");

        String path = scanner.next();
        scanner.close();
        Path logDir = Paths.get(path);
        //Path logDir = Paths.get("C:\\Users\\A\\Desktop\\LogParser\\logs");

        LogParser logParser = new LogParser(logDir);

        //For tests:
        String stringAfter = "29.2.2020 5:4:7";
        String stringBefore = "30.08.2030 16:08:13";
        DateFormat df = new SimpleDateFormat("d.M.y H:m:s");
        Date dateAfter = df.parse(stringAfter);
        Date dateBefore = df.parse(stringBefore);

        System.out.println("getNumberOfUniqueIPs:   "+logParser.getNumberOfUniqueIPs(null,null));
        System.out.println("getNumberOfUniqueIPs:   "+logParser.getNumberOfUniqueIPs(dateAfter,null));
        System.out.println("getNumberOfUniqueIPs:   "+logParser.getNumberOfUniqueIPs(null,dateBefore));
        System.out.println("getNumberOfUniqueIPs:   "+logParser.getNumberOfUniqueIPs(dateAfter,dateBefore));

        // 127.0.0.1	    	30.08.2012  16:08:13	LOGIN	OK
        // 12.12.12.12	     	21.10.2021  19:45:25	SOLVE_TASK 18	OK
        // 120.120.120.122	    29.2.2028   5:4:7	SOLVE_TASK 18	OK
        /*
        Метод getIPsForUser(String, Date, Date) класса LogParser должен возвращать корректное множество уникальных IP
        адресов за период с переданной даты after по null для пользователя String user.


        Если параметр before равен null, то нужно обработать все записи, у которых дата больше или равна after.
        */

        System.out.println("getIPsForUser:  "+logParser.getIPsForUser("Amigo", dateAfter,dateBefore));
        System.out.println("getIPsForUser:  "+logParser.getIPsForUser("Amigo", null,dateBefore));
        System.out.println("getIPsForUser:  "+logParser.getIPsForUser("Amigo", dateAfter,null));
        System.out.println("getIPsForUser:  "+logParser.getIPsForUser("Amigo", null,null));


        System.out.println("getIPsForEvent: "+logParser.getIPsForEvent(Event.LOGIN, dateAfter,dateBefore));


        System.out.println("getIPsForStatus: "+logParser.getIPsForStatus(Status.ERROR, dateAfter,dateBefore));

        System.out.println(logParser.getNumberOfUserEvents("Amigo", null,null));

        System.out.println(logParser.getAllSolvedTasksAndTheirNumber(null,null));


        System.out.println("getNumberOfAttemptToSolveTask: ");
        System.out.println(logParser.getNumberOfAttemptToSolveTask(18, null, null));

        System.out.println("getNumberOfSuccessfulAttemptToSolveTask: ");
        System.out.println(logParser.getNumberOfSuccessfulAttemptToSolveTask(15, null, null));
        System.out.println(logParser.getNumberOfSuccessfulAttemptToSolveTask(48, null, null));
        System.out.println(logParser.getNumberOfSuccessfulAttemptToSolveTask(15, null, null));
        System.out.println(logParser.getNumberOfSuccessfulAttemptToSolveTask(15, null, null));

    }
}