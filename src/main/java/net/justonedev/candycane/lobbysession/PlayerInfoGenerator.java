/* (C)2025 */
package net.justonedev.candycane.lobbysession;

import java.util.Random;

public final class PlayerInfoGenerator {
	private static final String[] ADJECTIVES = { "Adventurous", "Brave", "Clever", "Daring", "Energetic", "Fearless", "Generous", "Heroic", "Inventive",
			"Joyful", "Kind-hearted", "Loyal", "Mighty", "Noble", "Optimistic", "Playful", "Quick-witted", "Radiant", "Sincere", "Tenacious", "Unique",
			"Valiant", "Wise", "Xenial", "Youthful", "Zealous", "Lovely", "Charming", "Witty", "Dazzling", "Radiant", "Enchanting", "Magnificent",
			"Spectacular", };
	private static final String[] NOUNS = { "Explorer", "Guardian", "Hero", "Inventor", "Jester", "Knight", "Legend", "Mystic", "Nomad", "Oracle", "Paladin",
			"Questor", "Ranger", "Sage", "Titan", "Unicorn", "Vagabond", "Warrior", "Yeti", "Zephyr" };
	private static final String[] ANIMALS = { "Aardvark", "Bear", "Cheetah", "Dolphin", "Elephant", "Falcon", "Giraffe", "Hawk", "Iguana", "Jaguar", "Kangaroo",
			"Lion", "Monkey", "Narwhal", "Ocelot", "Penguin", "Quokka", "Raccoon", "Shark", "Tiger", "Uakari", "Vulture", "Walrus", "Xerus", "Yak", "Zebra" };
	private static final String[] ARTISTS = { "Picasso", "Van Gogh", "Da Vinci", "Monet", "Rembrandt", "Dali", "Kandinsky", "Warhol", "Matisse", "Pollock",
			"Kahlo", "Hokusai", "Michelangelo", "Cezanne", "O'Keeffe", "Renoir", "Basquiat", "Botticelli", "Chagall", "Gauguin", "Hopper", "Lichtenstein",
			"Munch", "Rothko", "Soutine" };
	private static final String[] COLORS = { "Red", "Blue", "Green", "Yellow", "Purple", "Orange", "Pink", "Brown", "Black", "White", "Gray", "Cyan",
			"Magenta", "Teal", "Lavender", "Coral", "Turquoise", "Crimson", "Gold", "Silver", "Bronze", "Ivory", "Emerald", "Ruby", "Sapphire" };
	private static final String[] FOODS = { "Pizza", "Burger", "Pasta", "Sushi", "Tacos", "Salad", "Ice Cream", "Cake", "Chocolate", "Cookies", "Brownies", "Cupcakes",
			"Donuts", "Pancakes", "Waffles", "Sandwiches", "Fries", "Noodles", "Steak", "Seafood", "Fruit Salad" };
	private static final String[] PLACES = { "Paris", "Tokyo", "New York", "London", "Sydney", "Rome", "Berlin", "Barcelona", "Amsterdam", "Dubai", "Istanbul", "Rio de Janeiro",
			"Bangkok", "Singapore", "Los Angeles", "Toronto", "Moscow", "Seoul", "Hong Kong", "Mexico City" };
	private static final String[] HOBBIES = { "Reading", "Traveling", "Cooking", "Gardening", "Photography", "Painting", "Writing", "Hiking", "Cycling", "Gaming",
			"Swimming", "Dancing", "Singing", "Knitting", "Fishing", "Camping", "Drawing", "Crafting", "Yoga", "Martial Arts" };
	private static final String[] MINECRAFT_MOBS = { "Zombie", "Skeleton", "Creeper", "Enderman", "Spider", "Slime", "Ghast", "Blaze", "Witch", "Villager", "Piglin", "Ender Dragon",
			"Wither", "Guardian", "Shulker", "Drowned", "Phantom", "Husk", "Stray", "Evoker" };
	private static final String[] MINECRAFT_BLOCKS = { "Dirt", "Stone", "Wood", "Grass", "Sand", "Cobblestone", "Brick", "Glass", "Iron", "Gold", "Diamond", "Emerald",
			"Obsidian", "Lapis Lazuli", "Quartz", "Netherite", "Redstone", "Clay", "Terracotta", "Concrete" };
	private static final String[] MINECRAFT_ITEMS = { "Sword", "Pickaxe", "Axe", "Shovel", "Bow", "Arrow", "Shield", "Armor", "Food", "Potion", "Map", "Compass",
			"Fishing Rod", "Flint and Steel", "Bed", "Bucket", "Minecart", "Boat", "Firework Rocket", "Totem of Undying" };
	private static final String[] ELDEN_RING_BOSSES = { "Margit", "Godrick", "Rennala", "Radahn", "Rykard", "Malenia", "Mohg", "Astel", "Maliketh", "Radagon", "Elden Beast" };
	private static final String[] ELDEN_RING_ITEMS = { "Golden Seed", "Crimson Seed", "Rune Arc", "Flame, Grant Me Strength", "Bloodhound's Step", "Hoarah Loux's Earthshaker",
			"Starscourge Greatsword", "Moonveil", "Rivers of Blood", "Malenia's Blade" };
	private static final String[] HARRY_POTTER_CHARACTERS = { "Harry Potter", "Hermione Granger", "Ron Weasley", "Albus Dumbledore", "Severus Snape", "Draco Malfoy", "Luna Lovegood", "Neville Longbottom",
			"Sirius Black", "Rubeus Hagrid", "Bellatrix Lestrange", "Voldemort" };
	private static final String[] HARRY_POTTER_SPELLS = { "Expelliarmus", "Lumos", "Alohomora", "Expecto Patronum", "Avada Kedavra", "Stupefy", "Accio", "Wingardium Leviosa",
			"Obliviate", "Sectumsempra", "Crucio", "Imperio", "Protego", "Riddikulus", "Apparate", "Portkey", "Diffindo", "Expulso", "Incendio", "Levioso" };
	private static final String[] HARRY_POTTER_CREATURES = { "Hippogriff", "Basilisk", "Thestral", "Acromantula", "Niffler", "Bowtruckle", "House Elf", "Dragon",
			"Griffin", "Chimera", "Mermaid", "Centaur", "Werewolf", "Vampire", "Boggart", "Dementor", "Thestral", "Fwooper", "Erumpent", "Nundu" };

	private static final String[][] ALL_CATEGORIES = { ARTISTS, FOODS, PLACES, HOBBIES, MINECRAFT_MOBS,
			MINECRAFT_BLOCKS, MINECRAFT_ITEMS, ELDEN_RING_BOSSES, ELDEN_RING_ITEMS, HARRY_POTTER_CHARACTERS,
			HARRY_POTTER_SPELLS, HARRY_POTTER_CREATURES };

	private final Random random;

	public PlayerInfoGenerator() {
		random = new Random(System.currentTimeMillis());
	}

	public String generateName() {
		double nameType = random.nextDouble();
		if (nameType < 0.33) {
			return ADJECTIVES[random.nextInt(ADJECTIVES.length)] + " " + ANIMALS[random.nextInt(ANIMALS.length)];
		} else if (nameType < 0.66) {
			String[] category = ALL_CATEGORIES[random.nextInt(ALL_CATEGORIES.length)];
			return category[random.nextInt(category.length)];
		} else {
			return ADJECTIVES[random.nextInt(ADJECTIVES.length)] + " " + NOUNS[random.nextInt(NOUNS.length)];
		}
	}

	public String generateColor() {
		return String.format("#%06X", random.nextInt(0xFFFFFF + 1));
	}

	public String generateUUID() {
		return String.format("%08X-%04X-%04X-%04X-%012X", random.nextInt(0x7FFFFFFF), random.nextInt(0xFFFF), random.nextInt(0xFFFF), random.nextInt(0xFFFF),
				random.nextLong() & 0xFFFFFFFFFFFFL);
	}
}
