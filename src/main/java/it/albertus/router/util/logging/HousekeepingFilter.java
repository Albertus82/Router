package it.albertus.router.util.logging;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import it.albertus.router.resources.Messages;
import it.albertus.router.util.FileSorter;
import it.albertus.util.logging.LoggerFactory;
import it.albertus.util.logging.TimeBasedRollingFileHandler;

public class HousekeepingFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(HousekeepingFilter.class);

	private static final int MIN_HISTORY = 1;

	private final int maxHistory;
	private final String datePattern;

	private String currentFileNamePart;

	private final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			try {
				return new SimpleDateFormat(datePattern);
			}
			catch (final RuntimeException e) {
				final String defaultDatePattern = TimeBasedRollingFileHandler.Defaults.DATE_PATTERN;
				logger.log(Level.WARNING, Messages.get("err.log.housekeeping.datePattern", datePattern, defaultDatePattern), e);
				return new SimpleDateFormat(defaultDatePattern);
			}
		}
	};

	public HousekeepingFilter(final int maxHistory) {
		this(maxHistory, TimeBasedRollingFileHandler.Defaults.DATE_PATTERN);
	}

	public HousekeepingFilter(final int maxHistory, final String datePattern) {
		if (maxHistory < MIN_HISTORY) {
			logger.warning(Messages.get("err.log.housekeeping.maxHistory", MIN_HISTORY));
			this.maxHistory = MIN_HISTORY;
		}
		else {
			this.maxHistory = maxHistory;
		}
		this.datePattern = datePattern;
		logger.config("Created new " + toString());
	}

	@Override
	public boolean isLoggable(final LogRecord record) {
		final String newFileNamePart = dateFormat.get().format(new Date());
		if (!newFileNamePart.equals(currentFileNamePart)) {
			int keep = this.maxHistory;
			if (currentFileNamePart == null) {
				keep++;
			}
			currentFileNamePart = newFileNamePart;
			deleteOldLogs(keep);
		}
		return true;
	}

	private void deleteOldLogs(final int keep) {
		final File[] files = LogManager.listFiles();
		if (files != null && files.length > keep) {
			FileSorter.sortByLastModified(files);
			for (int i = 0; i < files.length - keep; i++) {
				final boolean deleted = LogManager.deleteFile(files[i]);
				if (deleted) {
					logger.info(Messages.get("msg.log.file.deleted", files[i]));
				}
			}
		}
	}

	public int getMaxHistory() {
		return maxHistory;
	}

	public String getDatePattern() {
		return datePattern;
	}

	@Override
	public String toString() {
		return "HousekeepingFilter [maxHistory=" + maxHistory + ", datePattern=" + datePattern + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((datePattern == null) ? 0 : datePattern.hashCode());
		result = prime * result + maxHistory;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof HousekeepingFilter)) {
			return false;
		}
		HousekeepingFilter other = (HousekeepingFilter) obj;
		if (datePattern == null) {
			if (other.datePattern != null) {
				return false;
			}
		}
		else if (!datePattern.equals(other.datePattern)) {
			return false;
		}
		if (maxHistory != other.maxHistory) {
			return false;
		}
		return true;
	}

}
