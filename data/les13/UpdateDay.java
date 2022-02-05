package academy.kafka;

import java.time.LocalDate;

import academy.kafka.entities.Day;

public class UpdateDay extends Thread {
  final int startyear;
  final int pause;

  public UpdateDay(int pause, int startyear) {
    this.pause = pause;
    this.startyear = startyear;
  }

  public void run() {
    System.out.println("inserting days from year:" + startyear);
   
    int lastYear = startyear;
   
    for (LocalDate date = LocalDate.of(startyear, 1, 1); date.isBefore(LocalDate.now()); date = date.plusDays(1)) {
      if (pause > 0)
        try {
          sleep(pause);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      int year = date.getYear();
      if (year > lastYear) {
        lastYear = year;
        System.out.println("inserting year:" + year);
      }
      academy.kafka.GenerateData.insertDay(new Day(date));
    }
  }

}