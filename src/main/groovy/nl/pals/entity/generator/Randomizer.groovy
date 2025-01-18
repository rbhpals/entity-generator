package nl.pals.entity.generator
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class Randomizer {

	
	static random(options) {
		def random = new Random()
		options[random.nextInt(options.size())]
	}

	static random(options, weights) {
		if (options.size() != weights.size()) {
			throw new IllegalArgumentException("options and weights must have the same size.")
		}

		// Calculate the total weight
		def totalWeight = weights.sum()

		// Create a cumulative weight list
		def cumulativeWeights = []
		def cumulativeSum = 0
		weights.each { weight ->
			cumulativeSum += weight
			cumulativeWeights << cumulativeSum
		}

		def random = new Random()

		// Generate the random string

		def randomValue = random.nextInt(totalWeight)
		def selectedIndex = cumulativeWeights.findIndexOf { it > randomValue }
		return options[selectedIndex]
	}



	static String randomDate(String startDateStr, String endDateStr) {
		LocalDate startDate = LocalDate.parse(startDateStr)
		LocalDate endDate = LocalDate.parse(endDateStr)
		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("Start date must be before end date.")
		}
		long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
		Random random = new Random();
		long randomDays = random.nextInt((int) daysBetween + 1); // +1 to include endDate

		// Return the random date
		return startDate.plusDays(randomDays).toString();
	}

	static String randomDate(LocalDate startDate, LocalDate endDate) {
		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("Start date must be before end date.")
		}
		long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
		Random random = new Random();
		long randomDays = random.nextInt((int) daysBetween + 1); // +1 to include endDate

		// Return the random date
		return startDate.plusDays(randomDays).toString();
	}

	static int randomInt(int min, int max) {
		def random = new Random()
		random.nextInt((max - min) + 1) + min
	}
	
	static int random(IntRange range) {
		def max = range.to
		def min = range.from
		def random = new Random()
		random.nextInt((max - min) + 1) + min
	}
}
