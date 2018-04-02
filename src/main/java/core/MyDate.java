package core;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;

public class MyDate implements Comparable<MyDate> {
	
	private static TreeSet<MyDate> dates_local = new TreeSet<>();
	private static TreeSet<MyDate> dates_int = new TreeSet<>();
	private static TreeSet<MyDate> dates_str = new TreeSet<>();
	
	private static DateTimeFormatter dateFormat_rss = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss",Locale.ENGLISH);
	private static DateTimeFormatter dateFormat_change = DateTimeFormatter.ofPattern("dd MM yyyy",Locale.ENGLISH);
	
	private static int type = -1;
	private static int type_config = -1;
	private static boolean lock = false;
	
	private LocalDate date_local;
	private Integer date_int;
	private String date_str;
	
	public MyDate(LocalDate date)
	{
		this.date_local = date;		
		this.date_str = date.format(dateFormat_change);
	}

	public MyDate(Integer i)
	{
		this.date_int = new Integer(i);
		this.date_str = ""+i;
	}

	public MyDate(String s)
	{
		this.date_str = s.trim();
		//if (type == -1)
		//{ // check the type of dates
			try {
				this.date_local = LocalDate.parse(this.date_str, dateFormat_change);
				type = 1;
			}
			catch (DateTimeParseException e)
			{
				try {
					this.date_int = Integer.parseInt(this.date_str);
					type = 2;
				}
				catch (NumberFormatException ne)
				{
					type = 3;
				}
			}
		/*}
		else
		try {
			this.date_str = s.trim();
			switch(type)
			{
			case 1:
				this.date_local = LocalDate.parse(this.date_str, dateFormat_change);
				break;
			case 2:
				this.date_int = Integer.parseInt(this.date_str);
				break;
			}
		} catch (Exception e)
		{
			System.out.println("Error: different format for the dates");
		}*/
	}
	
	public static int size()
	{
		switch(type)
		{
		case 1:
			return dates_local.size();
		case 2:
			return dates_int.size();
		case 3:
			return dates_str.size();
		}
		return 0;
	}

	public static void addDate_localdate(LocalDate d)
	{
		dates_local.add(new MyDate(d));
	}

	public static void addDate_string(String s)
	{
		dates_str.add(new MyDate(s));
	}

	public static void addDate_integer(Integer i)
	{
		dates_int.add(new MyDate(i));
	}
	
	public static void lock()
	{
		lock = true;
	}
	
	public static String addDate(String s)
	{
		if (lock)
			return s;
		if (type == -1)
		{ // check the type of dates
			try {
				addDate_localdate(LocalDate.parse(s.trim(), dateFormat_change));
				type = 1;
				type_config = 1;
			}
			catch (DateTimeParseException e)
			{
				try {
					addDate_integer(Integer.parseInt(s.trim()));
					type = 2;
					type_config = 2;
				}
				catch (NumberFormatException ne)
				{
					addDate_string(s.trim());
					type = 3;
					type_config = 3;
				}
			}
		}
		else
		switch(type_config)
		{
		case 1:
			addDate_localdate(LocalDate.parse(s.trim(), dateFormat_change));
			break;
		case 2:
			addDate_integer(Integer.parseInt(s.trim()));
			break;
		case 3:
			addDate_string(s.trim());
			break;			
		}
		return s;
	}
		
	public static String getTimePoint(String s)
	{
		if ((type_config == -1) || (type_config > 1))
			return s;
		String loc_date = s;
		if (s.startsWith("\""))
			loc_date = loc_date.split("\"")[1];
		loc_date = loc_date
    			.split("\\+")[0]
    			.split("\\-")[0]
    			.split("GMT")[0]
    			.trim();
		LocalDateTime d = LocalDateTime.parse(loc_date, dateFormat_rss);
		int i=0;
		Iterator<MyDate> iter = dates_local.iterator();
		while (iter.hasNext())
		{
			MyDate c = iter.next();
			if (d.isBefore(c.date_local.atStartOfDay()))
				return ""+i;
			i++;
		}
		return ""+i;
	}

	public int compareTo(MyDate d)
	{
		/*if (d.date_str.equals("all"))
			return 1;
		if (this.date_str.equals("all"))
			return -1;*/
		switch(type)
		{
		case 1:
			return this.date_local.compareTo(d.date_local);
		case 2:			
			if (d.date_int == null)
			{
				System.out.print("PB 1 avec " + d.date_str);
				System.exit(0);
			}
			if (date_int == null)
			{
				System.out.print("PB 2 avec " + date_str);
				System.exit(0);
			}
			return this.date_int.compareTo(d.date_int);
		case 3:
			return this.date_str.compareTo(d.date_str);
		}
		return 0;
	}
	
	public static String toprint()
	{
		String s = "Number of periods: ";
		switch(type)
		{
		case 1:
			s += dates_local.size();
			break;
		case 2:
			s += dates_int.size();
			break;
		case 3:
			s += dates_str.size();
			break;
		}
		return s;		
	}
	
	public int hashcode()
	{
		return date_str.hashCode();
	}
	
	public boolean equals(Object o)
	{
		return date_str.equals(o);
	}
	
	public String toString()
	{
		return date_str;
	}
		
}
