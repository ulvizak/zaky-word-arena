package com.example.data

data class WordOfTheDay(
    val word: String,
    val syllable: String,
    val partOfSpeech: String,
    val definition: String,
    val example: String,
    val origin: String,
    val funFact: String
)

data class TriviaQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val level: String = "Medium"
)

data class AnagramChallenge(
    val scrambled: String,
    val solution: String,
    val clue: String,
    val difficulty: String = "Easy"
)

data class CrosswordCell(
    val row: Int,
    val col: Int,
    val number: Int = 0, // Clue number (e.g. 1, 2, 3) shown in corner, 0 if none
    val isBlack: Boolean = false,
    val targetChar: Char = ' ',
    var enteredChar: String = ""
)

data class CrosswordClue(
    val id: String, // e.g. "1A", "2D"
    val number: Int,
    val direction: String, // "Across" or "Down"
    val clue: String,
    val row: Int,
    val col: Int,
    val length: Int,
    val answer: String
)

data class CrosswordPuzzle(
    val title: String,
    val topic: String,
    val size: Int,
    val cells: List<CrosswordCell>,
    val clues: List<CrosswordClue>
)

object Dictionary {
    val wordsOfTheDay = listOf(
        WordOfTheDay(
            word = "Ebullient",
            syllable = "e·bul·li·ent",
            partOfSpeech = "Adjective",
            definition = "Overflowing with enthusiasm, excitement, or vivacity; high-spirited.",
            example = "She gave an ebullient welcome to her long-lost friends at the gates.",
            origin = "From Latin ebullire, meaning 'to bubble up'.",
            funFact = "It shares roots with the word 'boil', representing literal bubbling energy!"
        ),
        WordOfTheDay(
            word = "Pernicious",
            syllable = "per·ni·cious",
            partOfSpeech = "Adjective",
            definition = "Having a harmful effect, especially in a gradual or subtle way.",
            example = "The pernicious influence of misleading information can destabilize local debates.",
            origin = "From Latin pernicies, meaning 'destruction'.",
            funFact = "Unlike 'toxic' which is immediate, pernicious implies damage that sneaks up silently over time."
        ),
        WordOfTheDay(
            word = "Kakistocracy",
            syllable = "kak·is·toc·ra·cy",
            partOfSpeech = "Noun",
            definition = "Government by the least suitable or competent citizens.",
            example = "Historians warned of a kakistocracy taking root when qualifications were ignored.",
            origin = "From Greek kakistos ('worst') and kratos ('rule').",
            funFact = "It was coined in 1829, during highly partisan congressional elections."
        ),
        WordOfTheDay(
            word = "Soliloquy",
            syllable = "so·lil·o·quy",
            partOfSpeech = "Noun",
            definition = "An act of speaking one's thoughts aloud when by oneself or regardless of any hearers.",
            example = "Hamlet's famous 'To be or not to be' is the most celebrated soliloquy in dramatic history.",
            origin = "From Latin solus ('alone') and loqui ('to speak').",
            funFact = "It is different from a monologue, which is addressed to other characters on stage."
        ),
        WordOfTheDay(
            word = "Sycophant",
            syllable = "syc·o·phant",
            partOfSpeech = "Noun",
            definition = "A person who acts obsequiously toward someone important in order to gain advantage.",
            example = "The director was surrounded by sycophants who laughed at every single unfunny joke.",
            origin = "From Greek sykophantes, literally meaning 'fig shower' or accuser.",
            funFact = "Ancient Greek law banned exporting figs, and some individuals blackmailed fig farmers, hence 'fig shower'!"
        )
    )

    val anagrams = listOf(
        AnagramChallenge("TENSELIM", "MILESTON", "A significant stage or event in development.", "Medium"),
        AnagramChallenge("TEACHERR", "RECHARTE", "Change the charter or grant a new constitution.", "Hard"),
        AnagramChallenge("SILENT", "LISTEN", "Give your full attention with your ears.", "Easy"),
        AnagramChallenge("BRAGED", "BADGER", "A striped nocturnal mammal, or to pester persistently.", "Easy"),
        AnagramChallenge("RESCUE", "SECURE", "To make safe or lock up firmly.", "Easy"),
        AnagramChallenge("DORMITORY", "DIRTYROOM", "Often found in student housing! (Two words, but combined here as dirtyroom)", "Hard"),
        AnagramChallenge("ASTRONOMER", "MOONSTARER", "A humorous term for someone observing celestial bodies.", "Hard"),
        AnagramChallenge("EARTHQUAKE", "QUEERTHAKA", "A violent shaking of the ground.", "Medium")
    )

    val triviaQuestions = listOf(
        TriviaQuestion(
            id = 1,
            question = "Which of these words means 'having a sweet, smooth, and flowing sound'?",
            options = listOf("Mellifluous", "Cacophonic", "Bellicose", "Garrulous"),
            correctIndex = 0,
            explanation = "Mellifluous comes from Latin mel (honey) and fluere (to flow). It describes sweet, smooth sounds, like melodic music.",
            level = "Medium"
        ),
        TriviaQuestion(
            id = 2,
            question = "Identify the word that means 'to speak in an evasive, misleading, or roundabout way'.",
            options = listOf("Prevaricate", "Gesticulate", "Ponder", "Vindicate"),
            correctIndex = 0,
            explanation = "To prevaricate means to speak evasively, avoiding a direct answer. It is essentially a polite word for beating around the bush.",
            level = "Hard"
        ),
        TriviaQuestion(
            id = 3,
            question = "What is the term for a word that is spelled the same backward as forward?",
            options = listOf("Palindrome", "Anagram", "Portmanteau", "Homophone"),
            correctIndex = 0,
            explanation = "Palindromes (like 'radar', 'kayak', or 'racecar') read the same forwards and backwards. Anagrams are rearranged letters.",
            level = "Easy"
        ),
        TriviaQuestion(
            id = 4,
            question = "Which portmanteau is created by blending the words 'Smoke' and 'Fog'?",
            options = listOf("Smog", "Sfog", "Fmoke", "Mist"),
            correctIndex = 0,
            explanation = "Smog is the famous blend (portmanteau) of Smoke and Fog, coined in the early 20th century to describe urban air pollution.",
            level = "Easy"
        ),
        TriviaQuestion(
            id = 5,
            question = "What does the Latin root 'Bene' mean in words like Benefit, Benevolent, and Benign?",
            options = listOf("Good / Well", "Bad / Evil", "Small", "Time"),
            correctIndex = 0,
            explanation = "'Bene' is the Latin root for good or well (opposite of 'Male' meaning bad, as in malevolent).",
            level = "Easy"
        ),
        TriviaQuestion(
            id = 6,
            question = "Which word describes a person who loves books and reading?",
            options = listOf("Bibliophile", "Bibliophobe", "Anglophile", "Philanthropist"),
            correctIndex = 0,
            explanation = "Bibliophile merges 'biblio' (book) and 'philos' (loving). A bookworm's formal title!",
            level = "Medium"
        ),
        TriviaQuestion(
            id = 7,
            question = "What is the meaning of the adjective 'Ephemeral'?",
            options = listOf("Lasting for a very short time", "Extremely heavy", "Perfectly clear", "Full of color"),
            correctIndex = 0,
            explanation = "Ephemeral describes something fleeting, brief, or lasting only one day (from Greek ephemeros, lasting a day).",
            level = "Medium"
        )
    )

    // Build default crossword levels
    // Let's create a perfect 5x5 Grid
    val defaultCrossword = CrosswordPuzzle(
        title = "The Quick Starter Grid",
        topic = "General Vocabulary",
        size = 5,
        cells = listOf(
            // Row 0
            CrosswordCell(0, 0, number = 1, isBlack = false, targetChar = 'S'),
            CrosswordCell(0, 1, number = 2, isBlack = false, targetChar = 'T'),
            CrosswordCell(0, 2, number = 3, isBlack = false, targetChar = 'A'),
            CrosswordCell(0, 3, number = 4, isBlack = false, targetChar = 'R'),
            CrosswordCell(0, 4, number = 0, isBlack = true,  targetChar = '#'),
            
            // Row 1
            CrosswordCell(1, 0, number = 5, isBlack = false, targetChar = 'H'),
            CrosswordCell(1, 1, number = 0, isBlack = false, targetChar = 'A'),
            CrosswordCell(1, 2, number = 0, isBlack = false, targetChar = 'S'),
            CrosswordCell(1, 3, number = 0, isBlack = false, targetChar = 'E'),
            CrosswordCell(1, 4, number = 6, isBlack = false, targetChar = 'S'),
            
            // Row 2
            CrosswordCell(2, 0, number = 7, isBlack = false, targetChar = 'A'),
            CrosswordCell(2, 1, number = 0, isBlack = false, targetChar = 'X'),
            CrosswordCell(2, 2, number = 0, isBlack = false, targetChar = 'H'),
            CrosswordCell(2, 3, number = 0, isBlack = false, targetChar = 'E'),
            CrosswordCell(2, 4, number = 0, isBlack = false, targetChar = 'S'),
            
            // Row 3
            CrosswordCell(3, 0, number = 8, isBlack = false, targetChar = 'P'),
            CrosswordCell(3, 1, number = 0, isBlack = false, targetChar = 'I'),
            CrosswordCell(3, 2, number = 0, isBlack = false, targetChar = 'E'),
            CrosswordCell(3, 3, number = 0, isBlack = true,  targetChar = '#'),
            CrosswordCell(3, 4, number = 0, isBlack = true,  targetChar = '#'),
            
            // Row 4
            CrosswordCell(4, 0, number = 0, isBlack = true,  targetChar = '#'),
            CrosswordCell(4, 1, number = 9, isBlack = false, targetChar = 'S'),
            CrosswordCell(4, 2, number = 0, isBlack = false, targetChar = 'S'),
            CrosswordCell(4, 3, number = 10,isBlack = false, targetChar = 'E'),
            CrosswordCell(4, 4, number = 0, isBlack = false, targetChar = 'T')
        ),
        clues = listOf(
            CrosswordClue("1A", 1, "Across", "A massive luminous ball of plasma in outer space.", 0, 0, 4, "STAR"),
            CrosswordClue("5A", 5, "Across", "Possesses or commands; experiences.", 1, 0, 4, "HASE"),
            CrosswordClue("7A", 7, "Across", "Chopping tools, plural of axe.", 2, 0, 5, "AXHES"),
            CrosswordClue("8A", 8, "Across", "A sweet or savory baked dish with crust.", 3, 0, 3, "PIE"),
            CrosswordClue("9A", 9, "Across", "A collection of matching objects; to place down.", 4, 1, 4, "SSET"),
            
            CrosswordClue("1D", 1, "Down", "A sharp, painful sensation.", 0, 0, 4, "SHAP"),
            CrosswordClue("2D", 2, "Down", "Drivers pay this on highways or bridges.", 0, 1, 4, "TAXIS"),
            CrosswordClue("3D", 3, "Down", "Warm, powdery residue left after a campfire.", 0, 2, 5, "ASHES"),
            CrosswordClue("4D", 4, "Down", "Slightly wet or damp (spelled creatively).", 0, 3, 3, "REE")
        )
    )

    // A list of general English words to validate collaborative inputs
    val validDictionary = setOf(
        "A", "AN", "AM", "AT", "AS", "BE", "BY", "HE", "HI", "IF", "IN", "IS", "IT", "ME", "MY", "NO", "OF", "ON", "OR", "SO", "TO", "UP", "WE",
        "ACT", "ACE", "ADD", "AGE", "AIM", "AIR", "ALE", "ALL", "AND", "ANT", "ANY", "APE", "APT", "ARC", "ARE", "ARK", "ARM", "ART", "ASH", "ASK", "ATE", "AWE", "AXE",
        "BAD", "BAG", "BAR", "BAT", "BEE", "BEG", "BET", "BID", "BIG", "BIN", "BIO", "BIT", "BOX", "BOY", "BUD", "BUG", "BUM", "BUS", "BUT", "BUY", "BYE",
        "CAB", "CAD", "CAM", "CAN", "CAP", "CAR", "CAT", "COB", "COD", "COG", "CON", "COW", "COY", "CRY", "CUB", "CUE", "CUP", "CUT",
        "DAB", "DAM", "DAY", "DEN", "DEW", "DID", "DIG", "DIM", "DIN", "DIP", "DOC", "DOG", "DON", "DOT", "DRY", "DUB", "DUE", "DUG", "DUE", "DYE",
        "EAR", "EAT", "EBB", "EGO", "ELF", "ELK", "END", "ERA", "ERR", "EVE", "EYE",
        "FAD", "FAN", "FAR", "FAT", "FED", "FEE", "FEN", "FEW", "FIB", "FIG", "FIN", "FIT", "FIX", "FLY", "FOB", "FOE", "FOG", "FOR", "FOX", "FRY", "FUN", "FUR",
        "GAB", "GAG", "GAP", "GAS", "GAY", "GEL", "GEM", "GET", "GIG", "GIN", "GLO", "GNU", "GOB", "GOD", "GUM", "GUN", "GUT", "GUY", "GYM",
        "HAD", "HAM", "HAS", "HAT", "HAY", "HEM", "HEN", "HER", "HEY", "HIC", "HID", "HIM", "HIP", "HIS", "HIT", "HOB", "HOD", "HOG", "HOP", "HOT", "HOW", "HUB", "HUG", "HUM", "HUT",
        "ICE", "IDY", "ILL", "INK", "INN", "ION", "IRE", "IRK", "ITS", "IVY",
        "JAR", "JAW", "JAY", "JET", "JEW", "JIB", "JIG", "JOB", "JOG", "JOT", "JOY", "JUG", "JUT",
        "KEG", "KEN", "KEY", "KID", "KIN", "KIT", "KOB",
        "LAB", "LAC", "LAD", "LAG", "LAP", "LAW", "LAY", "LEA", "LED", "LEG", "LEI", "LET", "LIB", "LID", "LIE", "LIP", "LIT", "LOB", "LOG", "LOP", "LOT", "LOW", "LOX", "LUG", "LUX",
        "MAD", "MAN", "MAP", "MAT", "MAW", "MAX", "MAY", "MEG", "MEN", "MET", "MID", "MID", "MIG", "MIX", "MOB", "MOD", "MOM", "MOP", "MOW", "MUD", "MUG", "MUM", "MUT",
        "NAB", "NAG", "NAP", "NAY", "NEB", "NET", "NEW", "NIB", "NIL", "NIP", "NOD", "NOG", "NOR", "NOT", "NOW", "NUN", "NUT",
        "OAF", "OAK", "OAR", "OAT", "OBI", "ODD", "ODE", "OFF", "OFT", "OHM", "OIL", "OLD", "ONE", "OPT", "ORB", "ORE", "ORG", "OUR", "OUT", "OWA", "OWL", "OWN", "PAD", "PAL", "PAN",
        "PAR", "PAT", "PAW", "PAY", "PEA", "PEC", "PED", "PEG", "PEN", "PEP", "PER", "PET", "PEW", "PHI", "PIE", "PIG", "PIN", "PIP", "PIT", "PLY", "POD", "POP", "POT", "PRO", "PRY",
        "PUG", "PUN", "PUP", "PUS", "PUT",
        "QIS", "QUA",
        "RAD", "RAG", "RAM", "RAN", "RAP", "RAT", "RAW", "RAY", "REB", "RED", "REF", "REG", "REI", "REP", "RES", "REV", "RIB", "RID", "RIG", "RIM", "RIP", "ROB", "ROD", "ROE", "ROT",
        "ROW", "RUB", "RUE", "RUG", "RUM", "RUN", "RUT", "RYE",
        "SAD", "SAG", "SAL", "SAP", "SAT", "SAW", "SAY", "SEA", "SEC", "SEE", "SEG", "SEN", "SET", "SEW", "SEX", "SHA", "SHE", "SHY", "SIB", "SIC", "SIP", "SIR", "SIS", "SIT", "SIX",
        "SKA", "SKI", "SKY", "SLY", "SOB", "SOD", "SOL", "SON", "SOP", "SOW", "SOY", "SPA", "SPY", "SUB", "SUE", "SUM", "SUN", "SUP",
        "TAB", "TAD", "TAG", "TAN", "TAP", "TAR", "TAT", "TAX", "TEA", "TED", "TEE", "TEN", "THE", "THY", "TIC", "TIE", "TIL", "TIN", "TIP", "TOE", "TOG", "TOM", "TON", "TOO", "TOP",
        "TOY", "TRY", "TUB", "TUG", "TUI", "TUN", "TUX", "TWO",
        "URD", "URN", "USE", "UTA", "VAC", "VAN", "VAT", "VET", "VEX", "VIA", "VIE", "VIG", "VIM", "VOW", "VOX",
        "WAD", "WAG", "WAN", "WAR", "WAS", "WAT", "WAX", "WAY", "WEB", "WED", "WEE", "WET", "WHO", "WHY", "WIG", "WIN", "WIS", "WIT", "WOE", "WOK", "WON", "WOO", "WOT", "WRY", "WYN",
        "XIS",
        "YAK", "YAM", "YAP", "YAW", "YEA", "YEN", "YEP", "YES", "YET", "YEW", "YIN", "YIP", "YOB", "YOM", "YON", "YOU", "YOW",
        "ZAG", "ZAP", "ZED", "ZEE", "ZEN", "ZIG", "ZIP", "ZOO",
        "TEAM", "MEAT", "TAME", "MATE", "META", "EAST", "SEAT", "SATE", "STAT", "TART", "SMART", "MASTER", "STADIUM", "STREET", "STREAM", "MIST", "STEAM", "SMILE", "SMILES",
        "GREAT", "GRATE", "LEARN", "STORE", "SCORE", "ARENA", "WORDS", "GAMES", "DAILY", "STREAK", "KAHOOT", "POINTS"
    )
}
