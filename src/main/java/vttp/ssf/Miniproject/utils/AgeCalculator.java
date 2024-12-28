package vttp.ssf.Miniproject.utils;

import java.util.Calendar;
import java.util.Date;

public class AgeCalculator {

    public static int calculateAge(Date birthDate) {

        // Get the current date
        Calendar today = Calendar.getInstance();

        // Convert birthDate to a Calendar
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);

        // calculate the difference in years
        int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

        // Adjust if the birthday has not yet occurred this year
        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

}
