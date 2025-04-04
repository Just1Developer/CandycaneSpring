package net.justonedev.candycane.lobbysession;

import java.util.Random;

public final class PlayerInfoGenerator {
    private static final String[] ADJECTIVES = {
            "Adventurous", "Brave", "Clever", "Daring", "Energetic",
            "Fearless", "Generous", "Heroic", "Inventive", "Joyful",
            "Kind-hearted", "Loyal", "Mighty", "Noble", "Optimistic",
            "Playful", "Quick-witted", "Radiant", "Sincere", "Tenacious",
            "Unique", "Valiant", "Wise", "Xenial", "Youthful",
            "Zealous", "Lovely", "Charming", "Witty", "Dazzling",
            "Radiant", "Enchanting", "Magnificent", "Spectacular",
    };
    private static final String[] NOUNS = {
            "Explorer", "Guardian", "Hero", "Inventor", "Jester",
            "Knight", "Legend", "Mystic", "Nomad", "Oracle",
            "Paladin", "Questor", "Ranger", "Sage", "Titan",
            "Unicorn", "Vagabond", "Warrior", "Xplorer", "Yeti",
            "Zephyr"
    };
    private static final String[] ANIMALS = {
            "Aardvark", "Bear", "Cheetah", "Dolphin", "Elephant",
            "Falcon", "Giraffe", "Hawk", "Iguana", "Jaguar",
            "Kangaroo", "Lion", "Monkey", "Narwhal", "Ocelot",
            "Penguin", "Quokka", "Raccoon", "Shark", "Tiger",
            "Uakari", "Vulture", "Walrus", "Xerus", "Yak",
            "Zebra"
    };
    private static final String[] ARTISTS = {
            "Picasso", "Van Gogh", "Da Vinci", "Monet", "Rembrandt",
            "Dali", "Kandinsky", "Warhol", "Matisse", "Pollock",
            "Kahlo", "Hokusai", "Michelangelo", "Cezanne", "O'Keeffe",
            "Renoir", "Basquiat", "Botticelli", "Chagall", "Gauguin",
            "Hopper", "Lichtenstein", "Munch", "Rothko", "Soutine"
    };

    private final Random random;

    public PlayerInfoGenerator() {
        random = new Random(System.currentTimeMillis());
    }

    public String generateName() {
        double nameType = random.nextDouble();
        if (nameType < 0.33) {
            return ADJECTIVES[random.nextInt(ADJECTIVES.length)] + " " +
                    ANIMALS[random.nextInt(ANIMALS.length)];
        } else if (nameType < 0.66) {
            return ARTISTS[random.nextInt(ARTISTS.length)];
        } else {
            return ADJECTIVES[random.nextInt(ADJECTIVES.length)] + " " +
                    NOUNS[random.nextInt(NOUNS.length)];
        }
    }

    public String generateColor() {
        return String.format("#%06X", random.nextInt(0xFFFFFF + 1));
    }

    public String generateUUID() {
        return String.format("%08X-%04X-%04X-%04X-%012X",
                random.nextInt(0x7FFFFFFF),
                random.nextInt(0xFFFF),
                random.nextInt(0xFFFF),
                random.nextInt(0xFFFF),
                random.nextLong() & 0xFFFFFFFFFFFFL);
    }
}
