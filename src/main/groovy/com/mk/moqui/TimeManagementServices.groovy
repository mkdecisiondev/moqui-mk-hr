package com.mk.moqui
import org.moqui.context.ExecutionContext
import org.moqui.entity.EntityValue
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.text.DateFormat
import java.text.DecimalFormat
import java.time.LocalDate

class TimeAndAttendanceServices {
    static Map calculateSumOfHours(ExecutionContext ec) {

        double sumOfHours = 0
        int arraySize = ec.context.timeEntryList.size()


        Calendar c = GregorianCalendar.getInstance()

        // Getting the current time to be use later if there is a open time entry
        def timeNow = c.getTime()

//        System.out.println("Current week = " + Calendar.DAY_OF_WEEK)

        // Set the calendar to Monday of the current week
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
//        System.out.println("Current week = " + Calendar.DAY_OF_WEEK)

        // Print dates of the current week starting on Monday
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        String startDate = "", endDate = ""

        startDate = df.format(c.getTime()) + " 00:00:00.000"
        c.add(Calendar.DATE, 6)
        endDate = df.format(c.getTime()) + " 23:59:59.999"

//        System.out.println("Start Date = " + startDate)
//        System.out.println("End Date = " + endDate)

        SimpleDateFormat dateFormatForTS = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
        Date parsedStartDate = dateFormatForTS.parse(startDate)
        Timestamp timeStampStart = new java.sql.Timestamp(parsedStartDate.getTime())
        Date parsedEndDate = dateFormatForTS.parse(endDate)
        Timestamp timeStampEnd = new java.sql.Timestamp(parsedEndDate.getTime())

        for (int i =0; i < arraySize; i++) {
            EntityValue ev = ec.context.timeEntryList[i]

            if(ev.getPlainValueMap(0).fromDate > timeStampStart && ev.getPlainValueMap(0).thruDate < timeStampEnd && ev.getPlainValueMap(0).hours != null) {
//                println "Part of this week"
                sumOfHours = sumOfHours + ev.getPlainValueMap(0).hours
            }
            // Check if the open time entry doesn't have hours the logic below will act as a substitution
            else if(!ev.getPlainValueMap(0).hours) {
                // getting the milliseconds of the open time entry
                def openTimeFrom = ev.fromDate.getTime() 
                // getting the milliseconds of the current time
                def currentTime = timeNow.getTime() 
                // subtracting the open time entry to the current time
                def totalMilliSeconds = currentTime - openTimeFrom
                // converting the total to minutes
                def totalMinutes = totalMilliSeconds / 60000
                // converting the total to hours
                def totalInHours = totalMinutes / 60
                // adding the total to the sumOfHours
                sumOfHours+= totalInHours
            }
        }

        DecimalFormat df2 = new DecimalFormat("###.##")

        Map totalHours = ["sumOfHours":df2.format(sumOfHours)]

        return totalHours
    }

    static Map handleFilterDate(ExecutionContext ec) {
        String reportThru = ec.context.reportThru
        String reportFrom = ec.context.reportFrom
        String newReportThru
        String newReportFrom

        if (!reportThru){
            newReportThru = "9999-12-31"
        } else {
            newReportThru = LocalDate.parse(reportThru).plusDays(1).toString()
        }

        if (!reportFrom){
            newReportFrom = "1971-01-01"
        } else {
            newReportFrom = reportFrom
        }

        Map modifiedReportDates = ["modifiedReportFrom":newReportFrom, "modifiedReportThru":newReportThru]
        println "modifiedReportDates"
        println modifiedReportDates
        return modifiedReportDates
    }
}
