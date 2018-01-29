package utilClasses;

import java.util.Date;

public class Action {
    private String name;
    private Date date;
    private Event event;
    private Integer taskNumber;
    private Status status;


    public Action(String name, Date date, Event event, Integer taskNumber, Status status) {
        this.name = name;
        this.date = date;
        this.event = event;
        this.taskNumber = taskNumber;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public Event getEvent() {
        return event;
    }

    public Integer getTaskNumber() {
        return taskNumber;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Action{" +
                "name='" + name + '\'' +
                ", date=" + date +
                ", event=" + event +
                ", taskNumber=" + taskNumber +
                ", status=" + status +
                '}';
    }
}
