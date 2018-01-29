
import query.*;
import utilClasses.Action;
import utilClasses.Event;
import utilClasses.Status;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class LogParser implements IPQuery, UserQuery, DateQuery, EventQuery {
    private Path logDir;
    private static TreeMap<String, ArrayList<Action>> ipMap = new TreeMap<>();

    private static final DateFormat df = new SimpleDateFormat("d.M.y H:m:s", Locale.ENGLISH);

    public LogParser(Path logDir) {
        this.logDir = logDir;
        //ArrayList<String> logFiles = .stream().collect(Collectors.toCollection(ArrayList::new));
        List<File> logFiles = LogParser.readFiles(logDir);
        LogParser.parseLines(logFiles);

        /*ipMap.forEach((key, value) -> {
            System.out.println("IP : " + key + "\t\t" + "Actions : ");
            for (Action action : value) {
                System.out.println(action);
            }
        });*/

    }

    private static Date dateParsing(String string) {
        try {
            return df.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<File> readFiles(Path logDir) {
        try {
            List<File> logFiles = Files.walk(logDir)
                    .filter(Files::isRegularFile)  //check this s file
                    .map(Path::toFile)  //convert to file
                    .filter(file -> file.getName().endsWith(".log")) //check .log
                    .collect(Collectors.toList());
            return logFiles;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void parseLines(List<File> logFiles) {
        try {
            for (File file : logFiles) {
                /*//Вывод строк в консоль
                System.out.println("Strings in list: " + file.getName());
                Files.lines(file.toPath(), StandardCharsets.UTF_8).forEach(System.out::println);*/

                //Парсим строки
                String line;
                InputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis, Charset.defaultCharset());
                BufferedReader br = new BufferedReader(isr);
                while ((line = br.readLine()) != null) {
                    String[] attributes = line.split("\t");
                    String ip = attributes[0];
                    String name = attributes[1];
                    Date date = dateParsing(attributes[2]);

                    //Parsing event end taskNumber
                    String[] tempArrToParse = attributes[3].split(" ");
                    // Event
                    Event event = Event.valueOf(tempArrToParse[0]);
                    // taskNumber
                    Integer taskNumber;
                    if (tempArrToParse.length > 1)
                        taskNumber = Integer.parseInt(tempArrToParse[1]);
                    else taskNumber = -1;
                    //Status
                    Status status = Status.valueOf(attributes[4]);
                    //Action
                    Action action = new Action(name, date, event, taskNumber, status);
                    //Add action to map:
                    ArrayList<Action> definitions = ipMap.get(ip);
                    if (definitions == null) {
                        definitions = new ArrayList<Action>();
                        ipMap.put(ip, definitions);
                    }
                    definitions.add(action);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method getNumberOfUniqueIPs(Date after, Date before) return amount of unique IP for the selected period (including border dates).
     * If "Date after" equals null, method chooses set from (-infinity, Date before];
     * If "Date before" equals null, method chooses set from [Date after, +infinity);
     * If both dates equals null, method chooses set from (-infinity, +infinity);
     */
    @Override
    public int getNumberOfUniqueIPs(Date after, Date before) {

        int count = 0;
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            ArrayList<Action> actions = entry.getValue();

            long actionsCount = actions.stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .count();
            if (actionsCount > 0)
                count++;
        }
        return count;

    }

    /**
     * Метод getUniqueIPs() должен возвращать множество, содержащее все не повторяющиеся IP. Тип в котором будем хранить IP будет String.
     */
    @Override
    public Set<String> getUniqueIPs(Date after, Date before) {
        HashSet<String> uniqueIPs = new HashSet<>();

        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            ArrayList<Action> actions = entry.getValue();
            long actionsCount = actions.stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .count();
            if (actionsCount > 0)
                uniqueIPs.add(entry.getKey());
        }

        return uniqueIPs;
    }

    /**
     * Метод getIPsForUser() должен возвращать IP, с которых работал переданный пользователь.
     */
    @Override
    public Set<String> getIPsForUser(String user, Date after, Date before) {
        HashSet<String> uniqueIPs = new HashSet<>();

        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            ArrayList<Action> actions = entry.getValue();
            long actionsCount = actions.stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> d.getName().equals(user))
                    .count();
            if (actionsCount > 0)
                uniqueIPs.add(entry.getKey());
        }

        return uniqueIPs;
    }

    /**
     * Метод getIPsForEvent() должен возвращать IP, с которых было произведено переданное событие.
     */
    @Override
    public Set<String> getIPsForEvent(Event event, Date after, Date before) {

        HashSet<String> uniqueIPs = new HashSet<>();

        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            ArrayList<Action> actions = entry.getValue();
            long actionsCount = actions.stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> d.getEvent().equals(event))
                    .count();
            if (actionsCount > 0)
                uniqueIPs.add(entry.getKey());
        }

        return uniqueIPs;
    }

    /**
     * Метод getIPsForStatus() должен возвращать IP, события с которых закончилось переданным статусом.
     */
    @Override
    public Set<String> getIPsForStatus(Status status, Date after, Date before) {
        HashSet<String> uniqueIPs = new HashSet<>();

        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            ArrayList<Action> actions = entry.getValue();
            long actionsCount = actions.stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> d.getStatus().equals(status))
                    .count();
            if (actionsCount > 0)
                uniqueIPs.add(entry.getKey());
        }

        return uniqueIPs;
    }

    /**
     * Method getAllUsers() return all unique users.
     */
    @Override
    public Set<String> getAllUsers() {
        HashSet<String> uniqueUsers = new HashSet<>();

        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            ArrayList<Action> actions = entry.getValue();
            Set<String> userNames = actions.stream()
                    .map(p -> p.getName())
                    .collect(Collectors.toSet());
            uniqueUsers.addAll(userNames);
        }

        return uniqueUsers;
    }

    /**
     * Метод getNumberOfUsers() должен возвращать количество уникальных пользователей.
     * If "Date after" equals null, method chooses set from (-infinity, Date before);
     * If "Date before" equals null, method chooses set from (Date after, +infinity);
     * If both dates equals null, method chooses set from (-infinity, +infinity);
     */
    @Override
    public int getNumberOfUsers(Date after, Date before) {
        HashSet<String> uniqueUsers = new HashSet<>();

        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            ArrayList<Action> actions = entry.getValue();
            Set<String> userNames = actions.stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .map(p -> p.getName())
                    .collect(Collectors.toSet());
            uniqueUsers.addAll(userNames);
        }
        return uniqueUsers.size();
    }

    /**
     * Метод getNumberOfUserEvents() должен возвращать количество событий от определенного пользователя.
     */
    @Override
    public int getNumberOfUserEvents(String user, Date after, Date before) {

        Set<Event> eventsFromUser = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            ArrayList<Action> actions = entry.getValue();

            Set<Event> userEventsFromThisIP = actions.stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(p -> p.getName().equals(user))
                    .map(p -> p.getEvent())
                    .collect(Collectors.toSet());


            eventsFromUser.addAll(userEventsFromThisIP);
        }

        return eventsFromUser.size();


    }

    /**
     * Method getUsersForIP() return Set<String> of users from specified IP.
     * Users can use different IPs.
     */
    @Override
    public Set<String> getUsersForIP(String ip, Date after, Date before) {
        Set<String> usersFromIP = new HashSet<>();

        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {

            if (entry.getKey().equals(ip)) {
                Set<String> userNames = entry.getValue().stream()
                        .filter(d -> {
                            if (before != null)
                                return !before.before(d.getDate());
                            else
                                return true;
                        })
                        .filter(d -> {
                            if (after != null)
                                return !after.after(d.getDate());
                            else
                                return true;
                        })
                        .map(p -> p.getName())
                        .collect(Collectors.toSet());
                usersFromIP.addAll(userNames);
            }
        }
        return usersFromIP;
    }

    /**
     * Метод getLoggedUsers() должен возвращать пользователей, которые были залогинены.
     */
    @Override
    public Set<String> getLoggedUsers(Date after, Date before) {
        Set<String> loggedUsers = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {

            Set<String> loggedUsersFromIP = entry.getValue().stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(ev -> ev.getEvent().equals(Event.LOGIN))
                    .map(p -> p.getName())
                    .collect(Collectors.toSet());
            loggedUsers.addAll(loggedUsersFromIP);
        }
        return loggedUsers;

    }

    /**
     * Метод getDownloadedPluginUsers() должен возвращать пользователей, которые скачали плагин.
     */
    @Override
    public Set<String> getDownloadedPluginUsers(Date after, Date before) {
        Set<String> downloadedPluginUsers = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {

            Set<String> downloadedPluginUsersFromIP = entry.getValue().stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(ev -> ev.getEvent().equals(Event.DOWNLOAD_PLUGIN))
                    .map(p -> p.getName())
                    .collect(Collectors.toSet());
            downloadedPluginUsers.addAll(downloadedPluginUsersFromIP);
        }
        return downloadedPluginUsers;
    }

    /**
     * Метод getWroteMessageUsers() должен возвращать пользователей, которые отправили сообщение.
     */
    @Override
    public Set<String> getWroteMessageUsers(Date after, Date before) {

        Set<String> writeMessageUsers = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {

            Set<String> writeMessageUsersFromIP = entry.getValue().stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(ev -> ev.getEvent().equals(Event.WRITE_MESSAGE))
                    .map(p -> p.getName())
                    .collect(Collectors.toSet());
            writeMessageUsers.addAll(writeMessageUsersFromIP);
        }
        return writeMessageUsers;
    }

    /**
     * Метод getSolvedTaskUsers(Date after, Date before) должен возвращать пользователей, которые решали любую задачу.
     */
    @Override
    public Set<String> getSolvedTaskUsers(Date after, Date before) {

        Set<String> solveTaskUsers = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {

            Set<String> solveTaskUsersFromIP = entry.getValue().stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(ev -> ev.getEvent().equals(Event.SOLVE_TASK))
                    .map(p -> p.getName())
                    .collect(Collectors.toSet());
            solveTaskUsers.addAll(solveTaskUsersFromIP);
        }
        return solveTaskUsers;
    }

    /**
     * Метод getSolvedTaskUsers(Date after, Date before, int task) должен возвращать пользователей, которые решали задачу с номером task.
     */
    @Override
    public Set<String> getSolvedTaskUsers(Date after, Date before, int task) {
        Set<String> solveTaskUsers = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {

            Set<String> solveTaskUsersFromIP = entry.getValue().stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(ev -> ev.getEvent().equals(Event.SOLVE_TASK))
                    .filter(taskNum -> taskNum.getTaskNumber().equals(task))
                    .map(p -> p.getName())
                    .collect(Collectors.toSet());
            solveTaskUsers.addAll(solveTaskUsersFromIP);
        }
        return solveTaskUsers;
    }

    /**
     * Метод getDoneTaskUsers(Date after, Date before) должен возвращать пользователей, которые решали любую задачу.
     */
    @Override
    public Set<String> getDoneTaskUsers(Date after, Date before) {
        Set<String> doneTaskUsers = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {

            Set<String> doneTaskUsersFromIP = entry.getValue().stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(ev -> ev.getEvent().equals(Event.DONE_TASK))
                    .map(p -> p.getName())
                    .collect(Collectors.toSet());
            doneTaskUsers.addAll(doneTaskUsersFromIP);
        }
        return doneTaskUsers;
    }

    /**
     * Метод getDoneTaskUsers(Date after, Date before, int task) должен возвращать пользователей, которые решали задачу с номером task.
     */
    @Override
    public Set<String> getDoneTaskUsers(Date after, Date before, int task) {
        Set<String> doneTaskUsers = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {

            Set<String> doneTaskUsersFromIP = entry.getValue().stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(ev -> ev.getEvent().equals(Event.DONE_TASK))
                    .filter(taskNum -> taskNum.getTaskNumber().equals(task))
                    .map(p -> p.getName())
                    .collect(Collectors.toSet());
            doneTaskUsers.addAll(doneTaskUsersFromIP);
        }
        return doneTaskUsers;
    }

    /**
     * Метод getDatesForUserAndEvent() должен возвращать даты, когда определенный пользователь произвел определенное событие.
     */
    @Override
    public Set<Date> getDatesForUserAndEvent(String user, Event event, Date after, Date before) {
        Set<Date> allDates = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {

            Set<Date> datesFromIP = entry.getValue().stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(ev -> ev.getEvent().equals(event))
                    .filter(name -> name.getName().equals(user))
                    .map(p -> p.getDate())
                    .collect(Collectors.toSet());
            allDates.addAll(datesFromIP);
        }
        return allDates;
    }

    /**
     * Метод getDatesWhenSomethingFailed() должен возвращать даты, когда любое событие не выполнилось (статус FAILED).
     */
    @Override
    public Set<Date> getDatesWhenSomethingFailed(Date after, Date before) {
        Set<Date> allDates = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {

            Set<Date> datesFromIP = entry.getValue().stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(ev -> ev.getStatus().equals(Status.FAILED))
                    .map(p -> p.getDate())
                    .collect(Collectors.toSet());
            allDates.addAll(datesFromIP);
        }
        return allDates;
    }

    /**
     * Метод getDatesWhenErrorHappened() должен возвращать даты, когда любое событие закончилось ошибкой (статус ERROR).
     */
    @Override
    public Set<Date> getDatesWhenErrorHappened(Date after, Date before) {
        Set<Date> allDates = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {

            Set<Date> datesFromIP = entry.getValue().stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(ev -> ev.getStatus().equals(Status.ERROR))
                    .map(p -> p.getDate())
                    .collect(Collectors.toSet());
            allDates.addAll(datesFromIP);
        }
        return allDates;
    }

    /**
     * Метод getDateWhenUserLoggedFirstTime() должен возвращать дату, когда пользователь залогинился впервые за указанный период. Если такой даты в логах нет - null.
     */
    @Override
    public Date getDateWhenUserLoggedFirstTime(String user, Date after, Date before) {

        TreeSet<Date> allDates = new TreeSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {

            Set<Date> datesFromIP = entry.getValue().stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(ev -> ev.getEvent().equals(Event.LOGIN))
                    .filter(nm -> nm.getName().equals(user))
                    .map(p -> p.getDate())
                    .collect(Collectors.toSet());
            allDates.addAll(datesFromIP);
        }
        if (allDates.isEmpty())
            return null;
        else
            return allDates.first();
    }

    /**
     * Метод getDateWhenUserSolvedTask() должен возвращать дату, когда пользователь впервые попытался решить определенную задачу.
     * Если такой даты в логах нет - null.
     */
    @Override
    public Date getDateWhenUserSolvedTask(String user, int task, Date after, Date before) {
        TreeSet<Date> allDates = new TreeSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {

            Set<Date> datesFromIP = entry.getValue().stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(ev -> ev.getEvent().equals(Event.SOLVE_TASK))
                    .filter(us -> us.getName().equals(user))
                    .filter(tsk -> tsk.getTaskNumber().equals(task))
                    .map(p -> p.getDate())
                    .collect(Collectors.toSet());
            allDates.addAll(datesFromIP);
        }
        if (allDates.isEmpty())
            return null;
        else
            return allDates.first();
    }

    /**
     * Метод getDateWhenUserDoneTask() должен возвращать дату, когда пользователь впервые решил определенную задачу.
     * Если такой даты в логах нет - null.
     */
    @Override
    public Date getDateWhenUserDoneTask(String user, int task, Date after, Date before) {
        TreeSet<Date> allDates = new TreeSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {

            Set<Date> datesFromIP = entry.getValue().stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(ev -> ev.getEvent().equals(Event.DONE_TASK))
                    .filter(us -> us.getName().equals(user))
                    .filter(tsk -> tsk.getTaskNumber().equals(task))
                    .map(p -> p.getDate())
                    .collect(Collectors.toSet());
            allDates.addAll(datesFromIP);
        }

        if (allDates.isEmpty())
            return null;
        else
            return allDates.first();

    }

    /**
     * Метод getDatesWhenUserWroteMessage() должен возвращать даты, когда пользователь написал сообщение.
     */
    @Override
    public Set<Date> getDatesWhenUserWroteMessage(String user, Date after, Date before) {
        Set<Date> allDates = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {

            Set<Date> datesFromIP = entry.getValue().stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(ev -> ev.getEvent().equals(Event.WRITE_MESSAGE))
                    .filter(nm -> nm.getName().equals(user))
                    .map(p -> p.getDate())
                    .collect(Collectors.toSet());

            allDates.addAll(datesFromIP);
        }
        return allDates;
    }

    /**
     * Метод getDatesWhenUserDownloadedPlugin() должен возвращать даты, когда пользователь скачал плагин.
     */
    @Override
    public Set<Date> getDatesWhenUserDownloadedPlugin(String user, Date after, Date before) {
        Set<Date> allDates = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {

            Set<Date> datesFromIP = entry.getValue().stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(ev -> ev.getEvent().equals(Event.DOWNLOAD_PLUGIN))
                    .filter(nm -> nm.getName().equals(user))
                    .map(p -> p.getDate())
                    .collect(Collectors.toSet());

            allDates.addAll(datesFromIP);
        }
        return allDates;

    }

    /**
     * Метод getNumberOfAllEvents() должен возвращать количество событий за указанный период.
     */
    @Override
    public int getNumberOfAllEvents(Date after, Date before) {
        Set<Event> eventsFromUser = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            ArrayList<Action> actions = entry.getValue();

            Set<Event> userEventsFromThisIP = actions.stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .map(p -> p.getEvent())
                    .collect(Collectors.toSet());
            eventsFromUser.addAll(userEventsFromThisIP);
        }
        return eventsFromUser.size();
    }

    /**
     * Метод getAllEvents() должен возвращать все события за указанный период.
     */
    @Override
    public Set<Event> getAllEvents(Date after, Date before) {
        Set<Event> eventsFromUser = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            ArrayList<Action> actions = entry.getValue();

            Set<Event> userEventsFromThisIP = actions.stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .map(p -> p.getEvent())
                    .collect(Collectors.toSet());
            eventsFromUser.addAll(userEventsFromThisIP);
        }
        return eventsFromUser;
    }

    /**
     * Метод getEventsForIP() должен возвращать события, которые происходили с указанного IP.
     */
    @Override
    public Set<Event> getEventsForIP(String ip, Date after, Date before) {
        Set<Event> eventsFromUser = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            if (entry.getKey().equals(ip)) {
                ArrayList<Action> actions = entry.getValue();

                Set<Event> userEventsFromThisIP = actions.stream()
                        .filter(d -> {
                            if (before != null)
                                return !before.before(d.getDate());
                            else
                                return true;
                        })
                        .filter(d -> {
                            if (after != null)
                                return !after.after(d.getDate());
                            else
                                return true;
                        })
                        .map(p -> p.getEvent())
                        .collect(Collectors.toSet());
                eventsFromUser.addAll(userEventsFromThisIP);
            }

        }
        return eventsFromUser;

    }

    /**
     * Метод getEventsForUser() должен возвращать события, которые инициировал
     * определенный пользователь.
     */
    @Override
    public Set<Event> getEventsForUser(String user, Date after, Date before) {
        Set<Event> eventsFromUser = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            ArrayList<Action> actions = entry.getValue();

            Set<Event> userEventsFromThisIP = actions.stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(usr -> usr.getName().equals(user))
                    .map(p -> p.getEvent())
                    .collect(Collectors.toSet());
            eventsFromUser.addAll(userEventsFromThisIP);

        }
        return eventsFromUser;
    }

    /**
     * Метод getFailedEvents() должен возвращать события, которые не выполнились.
     */
    @Override
    public Set<Event> getFailedEvents(Date after, Date before) {
        Set<Event> eventsFromUser = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            ArrayList<Action> actions = entry.getValue();
            Set<Event> userEventsFromThisIP = actions.stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(usr -> usr.getStatus().equals(Status.FAILED))
                    .map(p -> p.getEvent())
                    .collect(Collectors.toSet());
            eventsFromUser.addAll(userEventsFromThisIP);
        }
        return eventsFromUser;

    }

    /**
     * Метод getErrorEvents() должен возвращать события, которые завершились ошибкой.
     */
    @Override
    public Set<Event> getErrorEvents(Date after, Date before) {
        Set<Event> eventsFromUser = new HashSet<>();
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            ArrayList<Action> actions = entry.getValue();
            Set<Event> userEventsFromThisIP = actions.stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(usr -> usr.getStatus().equals(Status.ERROR))
                    .map(p -> p.getEvent())
                    .collect(Collectors.toSet());
            eventsFromUser.addAll(userEventsFromThisIP);
        }
        return eventsFromUser;
    }

    /**
     * Метод getNumberOfAttemptToSolveTask() должен возвращать количество попыток
     * решить определенную задачу.
     * <p>
     * Метод getNumberOfAttemptToSolveTask(int, Date, Date) класса LogParser
     * должен правильно возвращать количество попыток решить задачу с номером task за период с null по null.
     */
    @Override
    public int getNumberOfAttemptToSolveTask(int task, Date after, Date before) {
        int totalCount = 0;
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            ArrayList<Action> actions = entry.getValue();
            long count = actions.stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(usr -> {
                        if ((usr.getTaskNumber().equals(task)) && (usr.getEvent().equals(Event.SOLVE_TASK)))
                            return true;
                        else
                            return false;
                    })
                    .count();
            totalCount += count;
        }
        return totalCount;
    }

    /**
     * Метод getNumberOfSuccessfulAttemptToSolveTask() должен возвращать количество
     * успешных решений определенной задачи.
     * <p>
     * Метод getNumberOfSuccessfulAttemptToSolveTask(int, Date, Date) класса LogParser должен правильно возвращать
     * количество попыток решить задачу с номером task за период с null по null.
     */
    @Override
    public int getNumberOfSuccessfulAttemptToSolveTask(int task, Date after, Date before) {
        int totalCount = 0;
        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            ArrayList<Action> actions = entry.getValue();
            long count = actions.stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(usr -> usr.getEvent().equals(Event.DONE_TASK))
                    .filter(usr -> usr.getTaskNumber() >= 0)
                    .filter(usr -> usr.getTaskNumber().equals(task))
                    .count();
            totalCount += count;
        }
        return totalCount;
    }

    /**
     * Метод getAllSolvedTasksAndTheirNumber() должен возвращать мапу (номер_задачи :
     * количество_попыток_решить_ее).
     */
    @Override
    public Map<Integer, Integer> getAllSolvedTasksAndTheirNumber(Date after, Date before) {

        Map<Integer, Integer> temp = new HashMap<>();

        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            ArrayList<Action> actions = entry.getValue();
            actions.stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> d.getEvent().equals(Event.SOLVE_TASK))
                    .forEach(p -> {
                        if (temp.get(p.getTaskNumber()) == null)
                            temp.put(p.getTaskNumber(), 0);

                        temp.put(p.getTaskNumber(), (temp.get(p.getTaskNumber()) + 1));

                    });
        }
        return temp;
    }

    /**
     * Метод getAllDoneTasksAndTheirNumber() должен возвращать мапу (номер_задачи :
     * сколько_раз_ее_решили).
     */
    @Override
    public Map<Integer, Integer> getAllDoneTasksAndTheirNumber(Date after, Date before) {
        Map<Integer, Integer> temp = new HashMap<>();

        for (Map.Entry<String, ArrayList<Action>> entry : ipMap.entrySet()) {
            ArrayList<Action> actions = entry.getValue();
            actions.stream()
                    .filter(d -> {
                        if (before != null)
                            return !before.before(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> {
                        if (after != null)
                            return !after.after(d.getDate());
                        else
                            return true;
                    })
                    .filter(d -> d.getEvent().equals(Event.DONE_TASK))
                    .forEach(p -> {
                        if (temp.get(p.getTaskNumber()) == null)
                            temp.put(p.getTaskNumber(), 0);

                        temp.put(p.getTaskNumber(), (temp.get(p.getTaskNumber()) + 1));

                    });
        }
        return temp;
    }

}