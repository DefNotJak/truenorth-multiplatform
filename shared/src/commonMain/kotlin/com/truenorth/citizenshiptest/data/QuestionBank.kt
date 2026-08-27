package com.truenorth.citizenshiptest.data

/**
 * Content sourced from the official "Discover Canada" study guide (IRCC).
 * Fast-changing facts (current PM, Governor General) are flagged for a
 * re-check before every release — see questions 22 and 23.
 */
object QuestionBank {

    fun byIds(ids: List<Int>): List<Question> {
        val byId = all.associateBy { it.id }
        return ids.mapNotNull { byId[it] }
    }

    /**
     * Questions from [category] suitable for flashcard review. Excludes
     * True/False questions whose correct answer is "False" - a flashcard
     * showing just "ANSWER: False" doesn't teach the actual fact, and risks
     * being misremembered as the true statement. Those questions still work
     * fine as-is in Practice Test, where all options are shown.
     */
    fun flashcardEligible(category: Category): List<Question> {
        return all.filter { question ->
            question.category == category &&
                !(question.type == QuestionType.TRUE_FALSE && question.options[question.correctAnswerIndex] == "False")
        }
    }

    /**
     * Picks [count] questions for a practice test, at most one per topicGroupId,
     * so two variants of the same fact never appear in the same test. Which
     * variant is picked is random each time, so repeated attempts naturally
     * rotate through phrasings of the same fact. [categories] narrows the pool
     * to those categories (empty/null = all categories); [questionType] narrows
     * to just that type (null = mixed). If fewer than [count] topic groups match
     * the filters, every matching group is used and the result is shorter than
     * requested - callers should size their UI off the actual result, not [count].
     */
    fun customTestSet(
        count: Int,
        categories: Set<Category>? = null,
        questionType: QuestionType? = null,
        restrictToIds: Set<Int>? = null
    ): List<Question> {
        val pool = all.filter { question ->
            (categories.isNullOrEmpty() || question.category in categories) &&
                (questionType == null || question.type == questionType) &&
                (restrictToIds == null || question.id in restrictToIds)
        }
        return pool.groupBy { it.topicGroupId }
            .values
            .shuffled()
            .take(count)
            .map { group -> group.random() }
    }

    /** Count of distinct topic groups matching [categories]/[questionType], i.e. the max test size available. */
    fun matchingQuestionCount(categories: Set<Category>?, questionType: QuestionType?): Int {
        return all.filter { question ->
            (categories.isNullOrEmpty() || question.category in categories) &&
                (questionType == null || question.type == questionType)
        }.map { it.topicGroupId }.distinct().size
    }

    val all: List<Question> = listOf(
        // --- Rights & Responsibilities ---
        Question(
            id = 1,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.TRUE_FALSE,
            text = "Canada's national anthem is \"O Canada.\"",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "\"O Canada\" was proclaimed Canada's official national anthem in 1980."
        ),
        Question(
            id = 2,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In what year did the Canadian Charter of Rights and Freedoms become part of Canada's Constitution?",
            options = listOf("1867", "1931", "1982", "1960"),
            correctAnswerIndex = 2,
            explanation = "The Charter became part of the Constitution in 1982, when Canada patriated its Constitution from Britain. (Discover Canada, page 8.)",
            topicGroupId = "orig_dup_group_13"
        ),
        Question(
            id = 3,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.TRUE_FALSE,
            text = "Obeying the law is a responsibility of everyone in Canada.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Obeying the law is one of the key responsibilities of citizenship listed in Discover Canada, reflecting the rule of law: Canada's legal system applies equally to everyone, so following the law - even laws a person may personally disagree with - is expected of all citizens and residents."
        ),
        Question(
            id = 4,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is a responsibility of Canadian citizens?",
            flashcardText = "What is a responsibility of Canadian citizens?",
            options = listOf(
                "Owning property",
                "Serving on a jury when summoned",
                "Speaking both English and French",
                "Working for the federal government"
            ),
            correctAnswerIndex = 1,
            explanation = "Serving on a jury when legally summoned is one of the responsibilities of Canadian citizenship listed in Discover Canada. Jury duty supports the justice system's tradition of trial by one's peers, and a person who is summoned must attend unless formally excused by the court. (Discover Canada, page 9.)",
            topicGroupId = "orig_dup_group_31"
        ),
        Question(
            id = 5,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.TRUE_FALSE,
            text = "Freedom of religion is a fundamental freedom protected under the Canadian Charter of Rights and Freedoms.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Freedom of conscience and religion is one of the fundamental freedoms named in Section 2 of the Charter, alongside freedom of thought, belief, opinion and expression, peaceful assembly, and association. It guarantees Canadians can hold and practise their own religious beliefs, or none at all, without government interference. (Discover Canada, page 8.)"
        ),
        Question(
            id = 6,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What are Canada's two official languages?",
            options = listOf(
                "English and Spanish",
                "French and Indigenous languages",
                "English only",
                "English and French"
            ),
            correctAnswerIndex = 3,
            explanation = "English and French are Canada's two official languages, protected under the Official Languages Act. (Discover Canada, page 39.)"
        ),
        Question(
            id = 7,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.TRUE_FALSE,
            text = "Jury duty in Canada is entirely optional and can be declined for any reason.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Jury duty is a legal responsibility when a citizen is summoned; it can only be declined for a limited set of legally recognized reasons."
        ),
        Question(
            id = 43,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is a fundamental freedom guaranteed by the Canadian Charter of Rights and Freedoms?",
            flashcardText = "What is a fundamental freedom guaranteed by the Canadian Charter of Rights and Freedoms?",
            options = listOf(
                "Right to public housing",
                "Right to free healthcare",
                "Right to a driver's licence",
                "Freedom of peaceful assembly"
            ),
            correctAnswerIndex = 3,
            explanation = "Freedom of peaceful assembly is one of the fundamental freedoms guaranteed by the Charter, along with freedom of conscience, religion, thought, belief, expression, and association. (Discover Canada, page 8.)",
            topicGroupId = "orig_dup_group_23"
        ),
        Question(
            id = 44,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.TRUE_FALSE,
            text = "Permanent residents of Canada have the right to vote in federal elections.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Section 3 of the Charter grants the right to vote specifically to \"every citizen of Canada.\" Permanent residents can live, work, and pay taxes in Canada, but they must complete the citizenship process - including the residency requirement and the oath of citizenship - before they can vote in a federal election. (Discover Canada, page 30.)",
            topicGroupId = "orig_dup_group_15"
        ),
        Question(
            id = 45,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Under the Charter, equality rights protect Canadians from discrimination based on which of the following?",
            options = listOf(
                "Income level only",
                "Political opinion only",
                "Race, national or ethnic origin, colour, religion, sex, age, or mental or physical disability",
                "Place of birth within Canada only"
            ),
            correctAnswerIndex = 2,
            explanation = "Section 15 of the Charter guarantees equality before and under the law, explicitly listing race, national or ethnic origin, colour, religion, sex, age, and mental or physical disability as prohibited grounds of discrimination; courts have since recognized further \"analogous grounds,\" such as sexual orientation, under the same section."
        ),
        Question(
            id = 46,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.TRUE_FALSE,
            text = "One responsibility of Canadian citizenship is to help others in the community.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada lists helping others in the community - for example through volunteering with charities, schools, faith groups, or civic organizations - as one of the responsibilities that come with Canadian citizenship, alongside obeying the law and voting. (Discover Canada, page 9.)",
            topicGroupId = "orig_dup_group_4"
        ),
        Question(
            id = 47,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which Charter right allows Canadian citizens to move to and live in any province they choose?",
            options = listOf("Equality rights", "Legal rights", "Language rights", "Mobility rights"),
            correctAnswerIndex = 3,
            explanation = "Mobility rights allow Canadian citizens to enter, remain in, and leave Canada, and to move to and take up residence in any province. (Discover Canada, page 8.)"
        ),
        Question(
            id = 48,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.TRUE_FALSE,
            text = "Only Canadian citizens can run for federal political office.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The Canada Elections Act restricts candidacy for federal office to Canadian citizens. Permanent residents do not yet hold the democratic rights guaranteed under Section 3 of the Charter, which are reserved for citizens, so they cannot run for the House of Commons or Senate.",
            topicGroupId = "orig_dup_group_15"
        ),
        Question(
            id = 49,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "According to Discover Canada, taking care of one's family is considered a ___ of citizenship.",
            options = listOf("Responsibility", "Legal right", "Federal service", "Optional courtesy"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada lists taking care of oneself and one's family as a basic responsibility of citizenship, reflecting the expectation that Canadians support their own households - for example through work - rather than relying solely on government assistance. (Discover Canada, page 9.)"
        ),
        Question(
            id = 50,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.TRUE_FALSE,
            text = "Permanent residents can be called for jury duty in Canada.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Jury duty is generally a responsibility of Canadian citizens; most provinces require jurors to be citizens.",
            topicGroupId = "orig_dup_group_31"
        ),
        Question(
            id = 51,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.TRUE_FALSE,
            text = "New citizens must give up their rights and citizenship from their country of origin to become Canadian.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Canada generally permits dual citizenship, so new citizens are not required to give up their citizenship of origin (though this can depend on the other country's own laws)."
        ),
        Question(
            id = 52,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is a legal right protected under the Charter?",
            flashcardText = "What is a legal right protected under the Charter?",
            options = listOf(
                "The right to free post-secondary tuition",
                "The right to a government job",
                "The right to be presumed innocent until proven guilty",
                "The right to own a business"
            ),
            correctAnswerIndex = 2,
            explanation = "Legal rights under the Charter include the presumption of innocence, the right to legal counsel, and protection against unreasonable search or seizure. (Discover Canada, page 36.)",
            topicGroupId = "orig_dup_group_14"
        ),
        Question(
            id = 53,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which right is reserved only for Canadian citizens, not permanent residents?",
            options = listOf(
                "The right to open a bank account",
                "The right to apply for a driver's licence",
                "The right to attend public school",
                "The right to vote and run for office"
            ),
            correctAnswerIndex = 3,
            explanation = "Democratic rights under Section 3 of the Charter - voting and running for office - apply only to Canadian citizens. This differs from services like banking, driving, or public education, which permanent residents can also access. (Discover Canada, page 30.)",
            topicGroupId = "orig_dup_group_15"
        ),
        Question(
            id = 54,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "According to Discover Canada, which of these is one of the key responsibilities of citizenship?",
            options = listOf(
                "Learning a third language",
                "Owning a home",
                "Voting in elections",
                "Travelling internationally"
            ),
            correctAnswerIndex = 2,
            explanation = "Voting in federal, provincial, and municipal elections is listed in Discover Canada as a key responsibility of citizenship, since elections are how Canadians choose their representatives and hold government accountable between elections. (Discover Canada, page 9.)"
        ),
        Question(
            id = 55,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following best reflects a core Canadian value described in Discover Canada?",
            flashcardText = "What is a core Canadian value described in Discover Canada?",
            options = listOf(
                "Mandatory military service",
                "One official religion for all citizens",
                "A single approved political party",
                "Equality of women and men"
            ),
            correctAnswerIndex = 3,
            explanation = "Discover Canada names equality of women and men as one of Canada's fundamental values, noting that Canadian law prohibits practices such as spousal abuse, \"honour killings,\" and forced marriage, and that men and women are equal under the law in all aspects of life. (Discover Canada, page 9.)",
            topicGroupId = "orig_dup_group_18"
        ),
        Question(
            id = 56,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does the rule of law mean in the Canadian context?",
            options = listOf(
                "No person or government is above the law, and all must obey it",
                "Only elected officials must obey the law",
                "Laws can be changed by any citizen at will",
                "The law applies only to newcomers"
            ),
            correctAnswerIndex = 0,
            explanation = "The rule of law is a foundational constitutional principle meaning government itself is bound by law rather than acting on arbitrary will, so elected officials, police, and ordinary citizens are all equally subject to Canada's laws and can be held accountable in court."
        ),
        Question(
            id = 57,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is a responsibility that helps protect Canada's environment for future generations?",
            flashcardText = "What is a responsibility that helps protect Canada's environment for future generations?",
            options = listOf(
                "Using only private vehicles",
                "Avoiding public parks",
                "Caring for and protecting Canada's heritage and environment",
                "Ignoring local bylaws"
            ),
            correctAnswerIndex = 2,
            explanation = "Discover Canada lists caring for and protecting Canada's heritage and environment - such as national parks, historic sites, and natural resources - among the responsibilities of citizenship, so these assets are preserved for future generations rather than depleted. (Discover Canada, page 9.)"
        ),
        Question(
            id = 58,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the Canadian Charter of Rights and Freedoms part of?",
            options = listOf(
                "The Constitution of Canada",
                "A provincial law only",
                "A municipal bylaw",
                "An international treaty only"
            ),
            correctAnswerIndex = 0,
            explanation = "The Charter is entrenched in the Constitution of Canada, giving it the highest legal authority in the country. (Discover Canada, page 8.)",
            topicGroupId = "orig_dup_group_13"
        ),
        Question(
            id = 59,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of these describes a democratic right protected by the Charter?",
            options = listOf(
                "The right of citizens to vote and to run for elected office",
                "The right to a free vehicle",
                "The right to skip jury duty without reason",
                "The right to unlimited free housing"
            ),
            correctAnswerIndex = 0,
            explanation = "Democratic rights under the Charter guarantee citizens the right to vote and to seek election to Parliament or a provincial legislature.",
            topicGroupId = "orig_dup_group_15"
        ),
        Question(
            id = 60,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What are language rights, as protected under the Charter, mainly concerned with?",
            options = listOf(
                "The right to a private translator at all times",
                "The right to refuse to learn any language",
                "The right to ban other languages from being spoken",
                "The right to use English or French in dealings with federal institutions"
            ),
            correctAnswerIndex = 3,
            explanation = "Sections 16 to 20 of the Charter establish English and French as Canada's official languages and give Canadians the right to communicate with, and receive services from, federal institutions in either official language. (Discover Canada, page 8.)"
        ),
        Question(
            id = 61,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which section of the Constitution recognizes and affirms existing Aboriginal and treaty rights?",
            options = listOf("Section 1", "Section 35", "Section 99", "Section 12"),
            correctAnswerIndex = 1,
            explanation = "Section 35 of the Constitution Act, 1982 recognizes and affirms existing Aboriginal and treaty rights. (Discover Canada, page 2.)"
        ),
        Question(
            id = 62,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "During the citizenship ceremony, new citizens take an oath that includes a pledge to do what?",
            options = listOf(
                "Be faithful and bear true allegiance to the reigning monarch, and to faithfully observe the laws of Canada",
                "Renounce all religious beliefs",
                "Pay a yearly citizenship fee",
                "Serve in the armed forces"
            ),
            correctAnswerIndex = 0,
            explanation = "The Oath of Citizenship includes pledging allegiance to the reigning monarch, as Canada's Head of State, and promising to fulfil duties as a Canadian citizen. (Discover Canada, page 2.)"
        ),
        Question(
            id = 63,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Freedom of peaceful assembly and freedom of association are both examples of what category of Charter rights?",
            options = listOf("Legal rights", "Fundamental freedoms", "Mobility rights", "Language rights"),
            correctAnswerIndex = 1,
            explanation = "Freedom of peaceful assembly and freedom of association are two of the four fundamental freedoms guaranteed in Section 2 of the Charter, alongside freedom of conscience and religion and freedom of thought, belief, opinion, and expression. (Discover Canada, page 8.)",
            topicGroupId = "orig_dup_group_23"
        ),
        Question(
            id = 64,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is generally considered a shared Canadian value, according to Discover Canada?",
            flashcardText = "What is a shared Canadian value, according to Discover Canada?",
            options = listOf(
                "Respect for individual human rights and freedoms",
                "Preference for one ethnic group over others",
                "Avoidance of civic participation",
                "Rejection of newcomers' traditions"
            ),
            correctAnswerIndex = 0,
            explanation = "Discover Canada identifies respect for individual human rights and freedoms - won through centuries of struggle in Canada's legal and political history - as one of the country's core shared values, later entrenched in the Canadian Charter of Rights and Freedoms."
        ),
        Question(
            id = 65,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Under the Charter's legal rights, a person who is arrested has the right to do what promptly?",
            options = listOf(
                "Contact the media",
                "Retain and instruct legal counsel",
                "Choose their own judge",
                "Avoid any questioning permanently"
            ),
            correctAnswerIndex = 1,
            explanation = "Anyone arrested or detained in Canada has the right to retain and instruct a lawyer without delay."
        ),
        Question(
            id = 66,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of these is an example of \"active citizenship\" described in Discover Canada?",
            options = listOf(
                "Ignoring election notices",
                "Avoiding all public meetings",
                "Refusing to pay taxes",
                "Volunteering in your community"
            ),
            correctAnswerIndex = 3,
            explanation = "Discover Canada highlights volunteering - for example with charities, community associations, or election campaigns - as a hallmark of \"active citizenship,\" since much of Canada's civic and community life depends on voluntary participation beyond simply obeying the law.",
            topicGroupId = "orig_dup_group_4"
        ),
        Question(
            id = 67,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the significance of Canadians being protected against \"unreasonable search or seizure\"?",
            options = listOf(
                "It means police can never search anyone",
                "It only applies to businesses",
                "It is a legal right under the Charter limiting when authorities can search a person or their property",
                "It applies only outside of Canada"
            ),
            correctAnswerIndex = 2,
            explanation = "Section 8 of the Charter protects everyone in Canada, citizens and non-citizens alike, from unreasonable search or seizure by the state. In practice, this generally requires police to have a warrant or other lawful grounds before searching a person, home, or property."
        ),
        Question(
            id = 68,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following best describes a citizen's responsibility toward Canadian laws?",
            flashcardText = "What is a citizen's responsibility toward Canadian laws?",
            options = listOf(
                "Following provincial laws but not federal ones",
                "Following only laws you agree with",
                "Obeying the law, even laws you personally disagree with",
                "Following laws only during elections"
            ),
            correctAnswerIndex = 2,
            explanation = "Discover Canada frames respect for the rule of law as including obedience to Canada's laws even when a person personally disagrees with them - disagreement is meant to be expressed through lawful means such as voting, advocacy, or challenging a law in court, not by ignoring it."
        ),
        Question(
            id = 69,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is one way Canadians are encouraged to participate in the democratic process, besides voting?",
            options = listOf(
                "Only participating once every ten years",
                "Avoiding public discussion of politics entirely",
                "Getting involved in a local community or political organization",
                "Leaving all decisions to elected officials without engagement"
            ),
            correctAnswerIndex = 2,
            explanation = "Getting involved in community groups, school councils, or political organizations is one way Canadians take part in democracy beyond voting.",
            topicGroupId = "orig_dup_group_20"
        ),
        Question(
            id = 70,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is NOT one of the Charter's fundamental freedoms?",
            flashcardText = "Which right is NOT one of the Charter's fundamental freedoms?",
            options = listOf(
                "Freedom of peaceful assembly",
                "Freedom of conscience and religion",
                "Freedom of thought, belief, and expression",
                "Freedom to bear arms"
            ),
            correctAnswerIndex = 3,
            explanation = "Unlike some other countries, a general freedom to bear arms is not one of the fundamental freedoms listed in the Canadian Charter."
        ),
        Question(
            id = 71,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does respecting the rights and freedoms of others mean as a responsibility of citizenship?",
            options = listOf(
                "It applies only to government officials",
                "Only respecting the rights of people who share your background",
                "Recognizing that your own rights come with a duty to respect the equal rights of others",
                "It is optional depending on personal belief"
            ),
            correctAnswerIndex = 2,
            explanation = "Respecting the rights and freedoms of others is described as a responsibility that balances individual rights within a shared society. (Discover Canada, page 3.)"
        ),
        Question(
            id = 72,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In Canada, freedom of the press is best described as part of which category of rights?",
            options = listOf("Legal rights", "Mobility rights", "Fundamental freedoms", "Democratic rights"),
            correctAnswerIndex = 2,
            explanation = "Freedom of the press and other media of communication is named explicitly in Section 2 of the Charter as one of the fundamental freedoms, protecting journalists' and media outlets' ability to report and publish without government censorship."
        ),
        Question(
            id = 73,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Why is voting considered both a right and a responsibility in Canada?",
            options = listOf(
                "Because it is legally mandatory to vote in Canada",
                "Because participating in choosing representatives helps sustain democracy for everyone",
                "Because only property owners may vote",
                "Because it guarantees a specific election outcome"
            ),
            correctAnswerIndex = 1,
            explanation = "Voting is a right guaranteed by the Charter and is also encouraged as a civic responsibility that helps sustain a healthy democracy, even though it is not legally mandatory."
        ),
        Question(
            id = 74,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is an example of respecting the property of others, as a Canadian value?",
            flashcardText = "What is an example of respecting the property of others, as a Canadian value?",
            options = listOf(
                "Not damaging or taking things that belong to someone else",
                "Borrowing items without asking",
                "Ignoring \"no trespassing\" signs",
                "Parking anywhere regardless of signage"
            ),
            correctAnswerIndex = 0,
            explanation = "Discover Canada lists respect for the property of others - not damaging, taking, or trespassing on what belongs to someone else - as one of the everyday civic values expected of everyone living in Canada, alongside obeying traffic and municipal bylaws."
        ),
        Question(
            id = 75,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the main purpose of the Canadian Charter of Rights and Freedoms?",
            options = listOf(
                "To regulate international trade",
                "To set immigration quotas",
                "To determine provincial boundaries",
                "To protect the basic rights and freedoms of everyone in Canada"
            ),
            correctAnswerIndex = 3,
            explanation = "Adopted in 1982 as part of the Constitution Act, the Charter's main purpose is to protect the basic rights and freedoms of everyone in Canada from government overreach, covering fundamental freedoms, democratic rights, mobility rights, legal rights, equality rights, and language rights. (Discover Canada, page 8.)"
        ),
        Question(
            id = 76,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "According to Discover Canada, which of the following is a shared responsibility of all Canadians toward newcomers?",
            options = listOf(
                "Welcoming newcomers and helping them integrate into Canadian society",
                "Requiring newcomers to abandon their culture",
                "Limiting newcomers' access to public services",
                "Discouraging newcomers from participating in civic life"
            ),
            correctAnswerIndex = 0,
            explanation = "Discover Canada describes welcoming newcomers and helping them integrate - for example through settlement support, mentorship, and community involvement - as a shared responsibility of all Canadians, reflecting Canada's history of nation-building through immigration."
        ),
        Question(
            id = 77,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does \"presumption of innocence\" mean in the Canadian legal system?",
            options = listOf(
                "Only citizens receive the presumption of innocence",
                "A person accused of a crime must prove their own innocence first",
                "A person accused of a crime is considered innocent until proven guilty in a court of law",
                "It applies only to minor offences"
            ),
            correctAnswerIndex = 2,
            explanation = "The presumption of innocence, protected under Section 11(d) of the Charter, means the Crown (prosecution) bears the burden of proving guilt beyond a reasonable doubt before an independent court, rather than the accused having to prove their own innocence. (Discover Canada, page 36.)",
            topicGroupId = "orig_dup_group_14"
        ),
        Question(
            id = 78,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of these best reflects the Canadian value of gender equality described in Discover Canada?",
            options = listOf(
                "Women and men are equal under Canadian law and have the same rights and opportunities",
                "Only men may hold public office",
                "Women were never granted the right to vote in Canada",
                "Gender equality applies only in the workplace"
            ),
            correctAnswerIndex = 0,
            explanation = "Discover Canada states that men and women are equal under Canadian law, with the same rights and opportunities in areas such as work, education, and public life - a principle reinforced by the Charter's equality guarantee against discrimination based on sex. (Discover Canada, page 9.)",
            topicGroupId = "orig_dup_group_18"
        ),
        Question(
            id = 79,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is one reason Canadians are encouraged to report for jury duty when summoned?",
            options = listOf(
                "It is the only way to avoid paying taxes",
                "A fair trial system depends on citizens being willing to serve as jurors",
                "It replaces the need to vote",
                "It is required only of permanent residents"
            ),
            correctAnswerIndex = 1,
            explanation = "Jury duty is a civic responsibility that helps ensure the justice system can provide fair trials."
        ),
        Question(
            id = 80,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which best describes how the Charter treats freedom of expression?",
            options = listOf(
                "It was removed from the Charter in 1982",
                "It has no limits whatsoever under any circumstance",
                "It applies only to elected officials",
                "It is a protected fundamental freedom, though it can be subject to reasonable limits set out by law"
            ),
            correctAnswerIndex = 3,
            explanation = "Freedom of expression is a protected fundamental freedom, but like other Charter rights, it can be subject to reasonable limits that are demonstrably justified in a free and democratic society. (Discover Canada, page 8.)"
        ),
        Question(
            id = 81,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is a practical example of \"obeying the law\" as a responsibility of citizenship?",
            options = listOf(
                "Only following laws that seem convenient",
                "Following traffic laws, such as stopping at red lights",
                "Following laws only when being observed",
                "Following laws only in your home province"
            ),
            correctAnswerIndex = 1,
            explanation = "Obeying the law means following Canada's laws consistently, not only when convenient or when someone is watching - stopping at red lights is a simple, everyday illustration of that ongoing duty every driver and citizen shares."
        ),
        Question(
            id = 82,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How often must federal elections be held in Canada, at a minimum, according to the Constitution?",
            options = listOf(
                "There is no legal maximum interval",
                "At least once every ten years",
                "At least once every two years",
                "At least once every five years"
            ),
            correctAnswerIndex = 3,
            explanation = "Section 4 of the Canadian Charter of Rights and Freedoms sets five years as the maximum gap allowed between federal elections, guaranteeing citizens a regular chance to hold their government accountable (elections are often called earlier)."
        ),
        Question(
            id = 83,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following best describes Canada's approach to freedom of religion?",
            flashcardText = "What is Canada's approach to freedom of religion?",
            options = listOf(
                "Only Christian denominations are legally protected",
                "Canada has one official state religion that all citizens must follow",
                "Canadians are free to follow any religion, or none at all",
                "Religious practice is banned in public spaces"
            ),
            correctAnswerIndex = 2,
            explanation = "Freedom of conscience and religion is a fundamental freedom, and Canada has no official state religion."
        ),
        Question(
            id = 84,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the relationship between rights and responsibilities described in Discover Canada?",
            options = listOf(
                "Rights are unlimited and have no accompanying responsibilities",
                "Canadian citizenship comes with both rights and responsibilities that go hand in hand",
                "Responsibilities apply only to permanent residents, not citizens",
                "Rights and responsibilities apply only during elections"
            ),
            correctAnswerIndex = 1,
            explanation = "Discover Canada frames citizenship as a two-way relationship: the rights citizens enjoy, such as legal protection and freedom, are paired with responsibilities like obeying the law, voting, and serving on a jury when summoned."
        ),
        Question(
            id = 85,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is an example of a civic responsibility beyond simply obeying the law?",
            flashcardText = "What is an example of a civic responsibility beyond simply obeying the law?",
            options = listOf(
                "Declining to ever attend community events",
                "Avoiding the news entirely",
                "Refusing to interact with local government",
                "Staying informed about, and participating in, Canadian society and politics"
            ),
            correctAnswerIndex = 3,
            explanation = "Beyond simply obeying the law, active citizenship means staying informed about public issues and taking part in community and political life - for example by voting, joining local groups, or attending civic events - which Discover Canada highlights as valued forms of participation.",
            topicGroupId = "orig_dup_group_20"
        ),

        // --- Who We Are ---
        Question(
            id = 8,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "According to Discover Canada, Aboriginal peoples in Canada include which three groups?",
            options = listOf(
                "Inuit and Métis only",
                "Only First Nations",
                "First Nations, Inuit, and Métis",
                "Settlers and Loyalists"
            ),
            correctAnswerIndex = 2,
            explanation = "The Constitution Act, 1982 (Section 35) formally recognizes three groups of Aboriginal peoples in Canada - First Nations, Inuit, and Metis - each with distinct histories, languages, and cultures. (Discover Canada, page 2.)",
            topicGroupId = "orig_dup_group_22"
        ),
        Question(
            id = 9,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Multiculturalism is protected under Canadian law.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The Canadian Multiculturalism Act (1988) formally recognizes and protects multiculturalism as a defining feature of Canada.",
            topicGroupId = "orig_dup_group_10"
        ),
        Question(
            id = 10,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Discover Canada describes the country's founding peoples as Aboriginal peoples, the French, and which other group?",
            options = listOf("The Spanish", "The British", "The Dutch", "The Portuguese"),
            correctAnswerIndex = 1,
            explanation = "Discover Canada names Aboriginal peoples, the French, and the British as Canada's three founding peoples, whose combined heritage shaped the country's laws, institutions, and bilingual character. (Discover Canada, page 10.)",
            topicGroupId = "orig_dup_group_26"
        ),
        Question(
            id = 11,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Canada's population is over 30 million people.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Canada's population has grown well past 30 million, and continues to grow through immigration."
        ),
        Question(
            id = 12,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "\"Métis\" refers to people with which ancestry?",
            options = listOf(
                "Recent immigrants from Europe",
                "French settlers only",
                "Mixed First Nations and European ancestry",
                "British settlers only"
            ),
            correctAnswerIndex = 2,
            explanation = "The Métis are a distinct people descended from mixed First Nations and European ancestry, with their own culture and history."
        ),
        Question(
            id = 13,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Bilingualism (English and French) is described as a fundamental characteristic of Canadian identity.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Bilingualism became official federal policy with the Official Languages Act of 1969, which gave English and French equal status in Parliament and federal institutions, reflecting Canada's French and British heritage. (Discover Canada, page 8.)"
        ),
        Question(
            id = 14,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which two European peoples most shaped Canada's early institutions and history?",
            options = listOf(
                "The Spanish and the Dutch",
                "The French and the British",
                "The Germans and the Italians",
                "The Portuguese and the French"
            ),
            correctAnswerIndex = 1,
            explanation = "New France, settled from the early 1600s, and British rule established after the 1763 Treaty of Paris together laid the legal, linguistic, and political foundations that shaped Canada's early development.",
            topicGroupId = "orig_dup_group_26"
        ),
        Question(
            id = 86,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Canada has historically welcomed immigrants from all over the world.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Since Confederation, Canada has drawn settlers and immigrants from around the world - from early European arrivals to more recent immigration from Asia, Africa, the Middle East, and Latin America - making openness to newcomers a continuous thread in its history. (Discover Canada, page 3.)",
            topicGroupId = "orig_dup_group_16"
        ),
        Question(
            id = 87,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What term refers to the traditional Arctic homeland of the Inuit in Canada?",
            options = listOf("Acadia", "New France", "Rupert's Land", "Inuit Nunangat"),
            correctAnswerIndex = 3,
            explanation = "Inuit Nunangat is the term for the Inuit homeland, spanning four regions across Canada's Arctic and sub-Arctic."
        ),
        Question(
            id = 88,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "First Nations peoples in Canada are a single, uniform culture that all share one language.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "First Nations in Canada are made up of many distinct nations, cultures, and languages, not a single uniform group."
        ),
        Question(
            id = 89,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which 19th-century wave of immigrants came to Canada fleeing a devastating famine in their home country?",
            options = listOf("Australian immigrants", "Irish immigrants", "Scandinavian immigrants", "Japanese immigrants"),
            correctAnswerIndex = 1,
            explanation = "Irish immigrants arrived in large numbers in the mid-1800s, many fleeing the Great Famine in Ireland."
        ),
        Question(
            id = 90,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Chinese immigrants were legally barred from any involvement in building Canada's transcontinental railway.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Thousands of Chinese labourers in fact played a major role building the Canadian Pacific Railway in the 1880s, often in dangerous conditions."
        ),
        Question(
            id = 91,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does the Canadian Multiculturalism Act (1988) do?",
            options = listOf(
                "Requires all immigrants to adopt a single national culture",
                "Recognizes and protects cultural diversity as a fundamental characteristic of Canadian society",
                "Restricts the number of cultures officially recognized in Canada",
                "Applies only to Indigenous peoples"
            ),
            correctAnswerIndex = 1,
            explanation = "Passed in 1988, the Canadian Multiculturalism Act made Canada the first country to adopt multiculturalism as official law, building on the 1971 federal multiculturalism policy and affirming that cultural diversity strengthens Canadian society. (Discover Canada, page 8.)",
            topicGroupId = "orig_dup_group_10"
        ),
        Question(
            id = 92,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Canada officially discourages immigrants from maintaining their own cultural traditions.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Canada's multiculturalism policy encourages newcomers to retain and share their cultural heritage while participating fully in Canadian society.",
            topicGroupId = "orig_dup_group_11"
        ),
        Question(
            id = 93,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which group of immigrants settled the Canadian Prairies in large numbers in the late 1800s and early 1900s?",
            options = listOf("Ukrainian immigrants", "Portuguese immigrants", "Greek immigrants", "Mexican immigrants"),
            correctAnswerIndex = 0,
            explanation = "Large numbers of Ukrainian immigrants settled the Prairie provinces beginning in the late 19th century, drawn by the offer of farmland."
        ),
        Question(
            id = 94,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "English and French are the two official languages recognized under Canada's Official Languages Act.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The Official Languages Act, 1969, established English and French as Canada's two official languages. (Discover Canada, page 39.)"
        ),
        Question(
            id = 95,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Most of Canada's population lives...",
            options = listOf(
                "In the far northern territories",
                "Close to the border with the United States",
                "Exclusively on the east coast",
                "Exclusively on the west coast"
            ),
            correctAnswerIndex = 1,
            explanation = "The large majority of Canadians live in a relatively narrow band close to the Canada-U.S. border, largely because the milder climate and more fertile land in that region made it the most practical for early settlement, farming, and the cities that grew from them."
        ),
        Question(
            id = 96,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Canada is one of the most densely populated countries in the world.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Despite being the second-largest country by area, Canada has a relatively small population, making it sparsely populated overall."
        ),
        Question(
            id = 97,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "The term \"Loyalists\" refers to which historical group?",
            options = listOf(
                "American colonists who remained loyal to the British Crown and moved to Canada after the American Revolution",
                "French settlers who supported independence from France",
                "British soldiers who settled permanently after the War of 1812",
                "Indigenous leaders who signed the early treaties"
            ),
            correctAnswerIndex = 0,
            explanation = "After the American Revolution ended in 1783, tens of thousands of colonists who had remained loyal to the British Crown left the newly independent United States and resettled in what is now Canada, especially in Ontario, Quebec, and the Maritimes - many later known as United Empire Loyalists. (Discover Canada, page 15.)"
        ),
        Question(
            id = 98,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "The Métis Nation is legally classified as identical to First Nations, with no distinct recognition.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "The Métis are constitutionally recognized as a distinct Aboriginal people, separate from First Nations, with their own culture and identity."
        ),
        Question(
            id = 99,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is one of Canada's three founding peoples, according to Discover Canada?",
            flashcardText = "Which group is one of Canada's three founding peoples, according to Discover Canada?",
            options = listOf("Norwegian settlers", "Dutch settlers", "Spanish colonizers", "Aboriginal peoples"),
            correctAnswerIndex = 3,
            explanation = "Aboriginal peoples - First Nations, Inuit, and Metis - lived in what is now Canada long before European contact, and Discover Canada recognizes them alongside the French and British as one of the country's three founding peoples.",
            topicGroupId = "orig_dup_group_26"
        ),
        Question(
            id = 100,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Non-status Indians are not recognized as \"Indians\" under the Indian Act, despite having Indigenous ancestry.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Under the Indian Act, only individuals formally registered with the federal government are legally recognized as status Indians; non-status Indians share Indigenous ancestry or identity but lack that registration, in some cases because of historical rules (later amended) that removed status under certain circumstances, such as marriage."
        ),
        Question(
            id = 101,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What historical event led to significant Vietnamese immigration to Canada in the late 1970s?",
            options = listOf(
                "The Vietnam War and the resulting refugee crisis",
                "A trade agreement between Canada and Vietnam",
                "A sports exchange program",
                "A university partnership"
            ),
            correctAnswerIndex = 0,
            explanation = "Following the Vietnam War, Canada welcomed tens of thousands of Vietnamese refugees, often called \"boat people.\". (Discover Canada, page 25.)"
        ),
        Question(
            id = 102,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Canada's identity is shaped only by British traditions, with little influence from other cultures.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Canadian identity draws on Indigenous, French, British, and many other cultural influences, reflecting its diversity."
        ),
        Question(
            id = 103,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of these best describes Canada's approach to newcomers, according to Discover Canada?",
            options = listOf(
                "Immigrants are kept separate from Canadian society",
                "Immigrants must abandon all prior customs",
                "Immigrants are encouraged to integrate while retaining their cultural heritage",
                "Immigrants are discouraged from becoming citizens"
            ),
            correctAnswerIndex = 2,
            explanation = "Canada's approach encourages integration alongside the retention of cultural heritage, rather than forced assimilation.",
            topicGroupId = "orig_dup_group_11"
        ),
        Question(
            id = 104,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Discover Canada divides the country into how many main geographic and cultural regions?",
            options = listOf("Five", "Two", "Ten", "Twelve"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada describes five main regions: the Atlantic Provinces, Central Canada, the Prairie Provinces, the West Coast, and the North."
        ),
        Question(
            id = 105,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Quebec is the only province where French is the majority language.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Quebec is Canada's only province with a French-speaking majority, and French is its sole official language."
        ),
        Question(
            id = 106,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is part of the Atlantic Provinces region described in Discover Canada?",
            flashcardText = "Which provinces make up the Atlantic Provinces region described in Discover Canada?",
            options = listOf(
                "Ontario and Quebec",
                "Nova Scotia, New Brunswick, Prince Edward Island, and Newfoundland and Labrador",
                "Alberta and Saskatchewan",
                "Yukon and Nunavut"
            ),
            correctAnswerIndex = 1,
            explanation = "The Atlantic Provinces - Nova Scotia, New Brunswick, Prince Edward Island, and Newfoundland and Labrador - form Canada's easternmost region, with economies historically tied to fishing and the sea; Newfoundland was the last of the four to join Confederation, in 1949. (Discover Canada, page 46.)"
        ),
        Question(
            id = 107,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Canada's North includes Yukon, the Northwest Territories, and Nunavut.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Canada's North consists of three territories - Yukon, the Northwest Territories, and Nunavut - which together cover a large share of Canada's land mass but hold a small fraction of its population, including many Inuit communities, especially in Nunavut."
        ),
        Question(
            id = 108,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which region of Canada is often associated with wheat farming and the energy industry?",
            options = listOf("The Prairie Provinces", "The Atlantic Provinces", "The West Coast", "The North"),
            correctAnswerIndex = 0,
            explanation = "The Prairie Provinces (Manitoba, Saskatchewan, and Alberta) are known for wheat farming and, particularly in Alberta, the energy industry. (Discover Canada, page 48.)"
        ),
        Question(
            id = 109,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Canada recognizes only one Indigenous language as officially protected nationwide.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Canada is home to many distinct Indigenous languages; no single Indigenous language holds exclusive national protection over the others."
        ),
        Question(
            id = 110,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is a key reason Canada is often described as a \"nation of immigrants\"?",
            options = listOf(
                "Canada does not allow permanent settlement",
                "All Canadians were born outside the country",
                "Aside from Indigenous peoples, most Canadians descend from families who immigrated over the past few centuries",
                "Immigration to Canada only began in the 21st century"
            ),
            correctAnswerIndex = 2,
            explanation = "Aside from Indigenous peoples, nearly all Canadians can trace their family history to an immigrant ancestor, from the original French and British settlers through successive waves of global immigration that continue today - making immigration central to the national story."
        ),
        Question(
            id = 111,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Canada's population is concentrated mainly in a few large urban centres.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "A large share of Canadians live in and around major cities such as Toronto, Montreal, and Vancouver."
        ),
        Question(
            id = 112,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following best describes religious diversity in Canada?",
            flashcardText = "How would you describe religious diversity in Canada?",
            options = listOf(
                "Canada has one official state religion",
                "Canadians practice a wide range of religions, and freedom of religion is constitutionally protected",
                "Religious practice is prohibited in Canada",
                "Only Christianity is legally recognized"
            ),
            correctAnswerIndex = 1,
            explanation = "Canada has no official state religion, and freedom of religion allows for a wide range of religious practice and belief. (Discover Canada, page 13.)"
        ),
        Question(
            id = 113,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "The Acadians were early French-speaking settlers who settled primarily in what is now British Columbia.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "The Acadians settled the Maritime region (present-day Nova Scotia and surrounding areas) beginning in the early 1600s, not British Columbia. (Discover Canada, page 11.)"
        ),
        Question(
            id = 114,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What happened to many Acadians in 1755, an event known as the Expulsion?",
            options = listOf(
                "They voluntarily relocated to France",
                "They were forcibly deported by the British",
                "They merged with the Iroquois Confederacy",
                "They founded the city of Toronto"
            ),
            correctAnswerIndex = 1,
            explanation = "In 1755, British authorities forcibly deported thousands of Acadians in an event known as the Great Expulsion. (Discover Canada, page 11.)"
        ),
        Question(
            id = 115,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "South Asian immigration to Canada has contributed significantly to the country's cultural diversity.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "South Asian communities, including from India, Pakistan, and Sri Lanka, have made significant and long-standing contributions to Canadian society."
        ),
        Question(
            id = 116,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is an example of an Indigenous peoples group recognized in Canada, alongside First Nations and Inuit?",
            flashcardText = "Which Indigenous peoples group is recognized in Canada, alongside First Nations and Inuit?",
            options = listOf("Acadians", "Métis", "Loyalists", "Huguenots"),
            correctAnswerIndex = 1,
            explanation = "The Métis, along with First Nations and Inuit, are one of the three constitutionally recognized Aboriginal peoples of Canada.",
            topicGroupId = "orig_dup_group_22"
        ),
        Question(
            id = 117,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Canada's multicultural character means there is no single, official Canadian ethnicity.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Canada's Multiculturalism Policy of 1971 and the later Canadian Multiculturalism Act of 1988 formally rejected the idea of a single official ethnicity, recognizing instead that Canadian identity is built from many cultural backgrounds living together under shared citizenship."
        ),
        Question(
            id = 118,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which city is often cited as one of the most ethnically diverse in the world?",
            options = listOf("Toronto", "Regina", "Charlottetown", "Fredericton"),
            correctAnswerIndex = 0,
            explanation = "Toronto is home to residents born in countries all over the world, with a very large share of its population born outside Canada, reflecting decades of high immigration to the city and making it one of the most ethnically diverse cities on the planet."
        ),
        Question(
            id = 119,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Caribana, a major Caribbean-Canadian cultural festival, is held annually in Vancouver.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Caribana is held annually in Toronto, not Vancouver, and is one of North America's largest Caribbean cultural festivals."
        ),
        Question(
            id = 120,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What best describes the relationship between French Canadian culture and Canadian identity overall?",
            options = listOf(
                "French is spoken only outside of Canada",
                "French Canadian culture has no influence on national identity",
                "French Canadian culture and the French language are core, founding elements of Canadian identity",
                "French Canadian culture emerged only after 2000"
            ),
            correctAnswerIndex = 2,
            explanation = "French Canadian culture and language are foundational to Canada's identity, reflected in its status as an official language and Quebec's distinct society. (Discover Canada, page 10.)"
        ),
        Question(
            id = 121,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Canada's bilingual character applies equally and identically in every province and territory.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "While English and French are Canada's official languages federally, their use and prevalence vary significantly by province and territory."
        ),
        Question(
            id = 122,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is true about Indigenous peoples' presence in Canada, according to Discover Canada?",
            flashcardText = "What is true about Indigenous peoples' presence in Canada, according to Discover Canada?",
            options = listOf(
                "Indigenous peoples lived in what is now Canada long before European explorers arrived",
                "Indigenous peoples arrived in Canada after European settlers",
                "Indigenous peoples are a recent addition to Canadian society",
                "Indigenous peoples originated in Europe"
            ),
            correctAnswerIndex = 0,
            explanation = "Indigenous peoples inhabited what is now Canada for thousands of years before European explorers and settlers arrived."
        ),
        Question(
            id = 123,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Canada's population today is shrinking because immigration has been banned.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Canada has not banned immigration; in fact, immigration is a major driver of the country's ongoing population growth."
        ),
        Question(
            id = 124,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following best reflects Canada's national identity, as described in Discover Canada?",
            flashcardText = "What does Canada's national identity, as described in Discover Canada, reflect?",
            options = listOf(
                "A shared citizenship built on diversity, freedom, and respect for all",
                "A single shared ethnicity for all citizens",
                "Loyalty to one political party",
                "A national identity defined solely by geography"
            ),
            correctAnswerIndex = 0,
            explanation = "Discover Canada describes Canadian identity as grounded not in a single ethnicity or culture but in shared citizenship, values such as freedom and equality, and respect for the country's diversity."
        ),
        Question(
            id = 125,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Hutterite and Mennonite communities are among the many distinct cultural communities found across Canada.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Canada is home to a wide range of distinct cultural and religious communities, including Hutterite and Mennonite communities, particularly on the Prairies."
        ),
        Question(
            id = 126,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What role did the fur trade play in the early relationship between Indigenous peoples and European settlers?",
            options = listOf(
                "It only involved European traders, with no Indigenous participation",
                "It had no impact on early Canadian society",
                "It was a foundation for early trade and alliances between Indigenous peoples and European traders",
                "It began after Confederation in 1867"
            ),
            correctAnswerIndex = 2,
            explanation = "The fur trade was central to early economic and diplomatic relationships between Indigenous peoples and European settlers, well before Confederation."
        ),
        Question(
            id = 127,
            category = Category.WHO_WE_ARE,
            type = QuestionType.TRUE_FALSE,
            text = "Canada's immigration system today accepts newcomers from only a small handful of countries.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Modern Canadian immigration draws from a broad range of source countries across every continent, not just a small handful.",
            topicGroupId = "orig_dup_group_16"
        ),
        Question(
            id = 128,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of these is a distinguishing feature of the Métis Nation's historical homeland?",
            options = listOf(
                "It is centred on the Canadian Prairies, particularly around the Red River area in Manitoba",
                "It is located entirely in the Arctic",
                "It is centred in Atlantic Canada",
                "It exists only in British Columbia"
            ),
            correctAnswerIndex = 0,
            explanation = "The Metis Nation emerged in the 18th and 19th centuries from unions between European fur traders and First Nations people, developing a distinct culture whose historical homeland is centred on the Red River Settlement near present-day Winnipeg, Manitoba. (Discover Canada, page 19.)"
        ),

        // --- Canada's History ---
        Question(
            id = 15,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In what year did Canada become a country through Confederation?",
            options = listOf("1776", "1812", "1867", "1931"),
            correctAnswerIndex = 2,
            explanation = "Canada was formed as a country on July 1, 1867, through Confederation."
        ),
        Question(
            id = 16,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "The War of 1812 was fought between the United States and Britain, with Canada as a British colony.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The War of 1812 saw American forces invade British North America; Canadian colonists helped repel the invasion. (Discover Canada, page 17.)",
            topicGroupId = "orig_dup_group_19"
        ),
        Question(
            id = 17,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which four provinces joined together to form Canada at Confederation in 1867?",
            options = listOf(
                "British Columbia, Alberta, Saskatchewan, Manitoba",
                "Ontario, Quebec, Nova Scotia, New Brunswick",
                "Newfoundland, PEI, Ontario, Quebec",
                "Only Ontario and Quebec"
            ),
            correctAnswerIndex = 1,
            explanation = "On July 1, 1867, the British North America Act united Ontario, Quebec, Nova Scotia, and New Brunswick into the Dominion of Canada - the event now celebrated annually as Canada Day. (Discover Canada, page 18.)",
            topicGroupId = "orig_dup_group_17"
        ),
        Question(
            id = 18,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "Canada fought as part of the Allied forces in both World War I and World War II.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Canada fought alongside Britain and its allies in both World Wars, with major contributions such as the capture of Vimy Ridge in 1917 and the landing at Juno Beach on D-Day in 1944 - sacrifices that helped shape Canada's growing international identity. (Discover Canada, page 23.)"
        ),
        Question(
            id = 19,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the name of the 1982 act that gave Canada full authority over its own Constitution?",
            options = listOf(
                "Treaty of Paris",
                "Statute of Westminster",
                "Constitution Act, 1982",
                "Balfour Declaration"
            ),
            correctAnswerIndex = 2,
            explanation = "The Constitution Act, 1982 \"patriated\" Canada's Constitution, giving Canada full legal authority to amend it without needing approval from Britain.",
            topicGroupId = "orig_dup_group_6"
        ),
        Question(
            id = 20,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "Sir John A. Macdonald was Canada's first Prime Minister.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Sir John A. Macdonald became Canada's first Prime Minister in 1867. (Discover Canada, page 19.)",
            topicGroupId = "orig_dup_group_5"
        ),
        Question(
            id = 21,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Confederation in 1867 was formally created by which piece of legislation?",
            options = listOf(
                "The Quebec Act",
                "The British North America Act, 1867",
                "The Royal Proclamation of 1763",
                "The Durham Report"
            ),
            correctAnswerIndex = 1,
            explanation = "The British North America Act, 1867 (now called the Constitution Act, 1867) created the Dominion of Canada. (Discover Canada, page 18.)"
        ),
        Question(
            id = 129,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who is credited with founding Quebec City in 1608?",
            options = listOf("John Cabot", "Jacques Cartier", "Louis Riel", "Samuel de Champlain"),
            correctAnswerIndex = 3,
            explanation = "Samuel de Champlain founded Quebec City in 1608, establishing a key settlement in New France. (Discover Canada, page 15.)"
        ),
        Question(
            id = 130,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "Jacques Cartier explored the St. Lawrence River in the 1530s and claimed the land for France.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Jacques Cartier explored the Gulf of St. Lawrence and the St. Lawrence River beginning in 1534, claiming the territory for France. (Discover Canada, page 14.)"
        ),
        Question(
            id = 131,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In what year did the British defeat the French at the Battle of the Plains of Abraham near Quebec City?",
            options = listOf("1867", "1812", "1759", "1608"),
            correctAnswerIndex = 2,
            explanation = "The Battle of the Plains of Abraham took place in 1759 and was a turning point in the British conquest of New France. (Discover Canada, page 15.)"
        ),
        Question(
            id = 132,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "Under the Treaty of Paris (1763), France ceded most of its North American territory to Britain.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The Treaty of Paris, 1763, ended the Seven Years' War and saw France cede most of New France to Britain.",
            topicGroupId = "orig_dup_group_3"
        ),
        Question(
            id = 133,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which act, passed in 1774, guaranteed French Canadians the right to their religion, language, and civil law under British rule?",
            options = listOf("The Constitution Act", "The Quebec Act", "The Statute of Westminster", "The Durham Report"),
            correctAnswerIndex = 1,
            explanation = "The Quebec Act of 1774 protected French civil law, the Catholic religion, and the French language for Canadiens under British rule. (Discover Canada, page 15.)"
        ),
        Question(
            id = 134,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "Loyalists who fled the American Revolution had no lasting influence on the development of Canada.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Loyalists had a major and lasting influence on Canada, shaping the development of Ontario, New Brunswick, and other regions."
        ),
        Question(
            id = 135,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "The War of 1812 was primarily fought between which two sides?",
            options = listOf(
                "The United States and Britain, with Canada as a British colony",
                "France and Britain",
                "Canada and Mexico",
                "Spain and the United States"
            ),
            correctAnswerIndex = 0,
            explanation = "The War of 1812 pitted the United States against Britain, with colonial and Indigenous forces helping defend British North America. (Discover Canada, page 17.)",
            topicGroupId = "orig_dup_group_19"
        ),
        Question(
            id = 136,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "General Sir Isaac Brock is remembered as a hero of the War of 1812.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Major-General Sir Isaac Brock is celebrated for his leadership defending Upper Canada during the War of 1812, though he was killed at the Battle of Queenston Heights. (Discover Canada, page 17.)"
        ),
        Question(
            id = 137,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who is remembered for warning British forces of an impending American attack during the War of 1812?",
            options = listOf("Laura Secord", "Louis Riel", "Emily Murphy", "Nellie McClung"),
            correctAnswerIndex = 0,
            explanation = "Laura Secord became a celebrated figure for her 1813 journey to warn British forces of an American attack. (Discover Canada, page 17.)"
        ),
        Question(
            id = 138,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "The Rebellions of 1837–38 in Upper and Lower Canada were peaceful protests that involved no armed conflict.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "The Rebellions of 1837–38 involved armed uprisings in both Upper and Lower Canada against colonial government. (Discover Canada, page 17.)"
        ),
        Question(
            id = 139,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What did the Durham Report, following the Rebellions of 1837–38, recommend?",
            options = listOf(
                "Abolishing all colonial legislatures",
                "Ending all British involvement in North America",
                "Returning the colonies to French control",
                "Granting responsible government to the Canadian colonies"
            ),
            correctAnswerIndex = 3,
            explanation = "The Durham Report recommended responsible government, where the executive must maintain the confidence of the elected assembly. (Discover Canada, page 17.)"
        ),
        Question(
            id = 140,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "The Fathers of Confederation met in Toronto to negotiate the terms of Canadian union.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "The key conferences leading to Confederation were held in Charlottetown and Quebec City, not Toronto."
        ),
        Question(
            id = 141,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who is often called the \"Father of Confederation\" for his leading role in negotiating Canada's union?",
            options = listOf(
                "William Lyon Mackenzie",
                "Louis Riel",
                "Lester B. Pearson",
                "Sir John A. Macdonald"
            ),
            correctAnswerIndex = 3,
            explanation = "Sir John A. Macdonald played a leading role in negotiating Confederation and is often called a \"Father of Confederation,\" alongside others such as George-Étienne Cartier. (Discover Canada, page 19.)"
        ),
        Question(
            id = 142,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "British Columbia joined Confederation in 1867, at the very founding of Canada.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "British Columbia joined Confederation in 1871, four years after Canada's founding, partly on the promise of a transcontinental railway.",
            topicGroupId = "orig_dup_group_25"
        ),
        Question(
            id = 143,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What was the name of the railway completed in 1885 that connected Canada from coast to coast?",
            options = listOf(
                "The Canadian Pacific Railway",
                "The Grand Trunk Highway",
                "The Trans-Canada Trail",
                "The National Railroad"
            ),
            correctAnswerIndex = 0,
            explanation = "Construction of the CPR was promised to persuade British Columbia to join Confederation in 1871, and the last spike was driven at Craigellachie, BC, in November 1885, linking the country by rail and opening the West to settlement. (Discover Canada, page 20.)"
        ),
        Question(
            id = 144,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "British Columbia became a province of Canada before Manitoba did.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Manitoba joined Confederation in 1870, one year before British Columbia joined in 1871.",
            topicGroupId = "orig_dup_group_25"
        ),
        Question(
            id = 145,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Louis Riel is most closely associated with which historical events?",
            options = listOf(
                "The Klondike Gold Rush",
                "The Battle of Vimy Ridge",
                "The Red River and North-West Resistances",
                "The Quiet Revolution"
            ),
            correctAnswerIndex = 2,
            explanation = "Louis Riel led the Red River Resistance of 1869–70 and the North-West Resistance of 1885, and is a central figure in Métis history. (Discover Canada, page 19.)"
        ),
        Question(
            id = 146,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "The Klondike Gold Rush of the late 1890s took place in Nova Scotia.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "The Klondike Gold Rush of 1896–1899 took place in the Yukon, not Nova Scotia. (Discover Canada, page 50.)"
        ),
        Question(
            id = 147,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which Canadian battle in World War I is considered a defining moment of national pride, where all four Canadian divisions fought together for the first time?",
            options = listOf(
                "The Battle of Batoche",
                "The Battle of Queenston Heights",
                "The Battle of the Plains of Abraham",
                "The Battle of Vimy Ridge"
            ),
            correctAnswerIndex = 3,
            explanation = "The Battle of Vimy Ridge in April 1917 saw all four Canadian divisions fight together for the first time, and is remembered as a defining national moment. (Discover Canada, page 21.)"
        ),
        Question(
            id = 148,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "Most women in Canada won the right to vote in federal elections in 1918.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Most women in Canada gained the right to vote in federal elections in 1918, following earlier provincial gains. (Discover Canada, page 21.)"
        ),
        Question(
            id = 149,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "The 1929 \"Persons Case\" resulted in what outcome?",
            options = listOf(
                "The Senate was abolished",
                "Indigenous peoples were granted the federal vote",
                "Canada gained full control over its Constitution",
                "Women were ruled to be legally considered \"persons\" eligible to sit in the Senate"
            ),
            correctAnswerIndex = 3,
            explanation = "The \"Famous Five\" Alberta women asked whether women were \"qualified persons\" eligible for Senate appointment; when the Supreme Court of Canada said no in 1928, they appealed to the Judicial Committee of the Privy Council in Britain, then Canada's highest court of appeal, which ruled in their favour in October 1929."
        ),
        Question(
            id = 150,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "The \"Famous Five\" were a group of women who led the fight in the Persons Case.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The Famous Five — Emily Murphy, Nellie McClung, Irene Parlby, Henrietta Muir Edwards, and Louise McKinney — led the Persons Case."
        ),
        Question(
            id = 151,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which Canadian province was the last to join Confederation, in 1949?",
            options = listOf("Prince Edward Island", "Newfoundland", "Saskatchewan", "Alberta"),
            correctAnswerIndex = 1,
            explanation = "Newfoundland (now Newfoundland and Labrador) joined Confederation in 1949, the last province to do so. (Discover Canada, page 19.)"
        ),
        Question(
            id = 152,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "Saskatchewan and Alberta joined Confederation in 1867, alongside Ontario and Quebec.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Saskatchewan and Alberta did not become provinces until 1905; only Ontario, Quebec, Nova Scotia, and New Brunswick joined in 1867. (Discover Canada, page 19.)",
            topicGroupId = "orig_dup_group_17"
        ),
        Question(
            id = 153,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Canadian forces played a major role in which Second World War operation on June 6, 1944?",
            options = listOf("The fall of Berlin", "The Battle of Britain", "D-Day, the invasion of Normandy", "Pearl Harbor"),
            correctAnswerIndex = 2,
            explanation = "Canadian troops landed at Juno Beach as part of the D-Day invasion of Normandy on June 6, 1944. (Discover Canada, page 23.)"
        ),
        Question(
            id = 154,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "Canada declared war on Germany separately from Britain in September 1939, reflecting its independent foreign policy.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Canada made its own separate declaration of war against Germany about a week after Britain, underscoring its independence in foreign affairs."
        ),
        Question(
            id = 155,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What was the Dieppe Raid of 1942?",
            options = listOf(
                "A Canadian naval victory in the Pacific",
                "A costly Allied raid on occupied France in which Canadian troops suffered heavy losses",
                "The final battle of the First World War",
                "A peace conference held in France"
            ),
            correctAnswerIndex = 1,
            explanation = "Nearly 5,000 Canadian troops made up most of the raiding force at Dieppe on August 19, 1942, and more than half were killed, wounded, or captured; the raid's costly failure taught lessons about amphibious assault planning that were later applied to the D-Day landings. (Discover Canada, page 23.)"
        ),
        Question(
            id = 156,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "The federal government has formally acknowledged the harm caused by the Indian Residential School system.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The Government of Canada issued a formal apology in 2008 for the harm caused by the Indian Residential School system. (Discover Canada, page 10.)"
        ),
        Question(
            id = 157,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What was the purpose of the Truth and Reconciliation Commission of Canada?",
            options = listOf(
                "To manage Canada's trade agreements",
                "To negotiate new provincial boundaries",
                "To oversee federal elections",
                "To document the history and impacts of the Indian Residential School system and promote reconciliation"
            ),
            correctAnswerIndex = 3,
            explanation = "Created under the Indian Residential Schools Settlement Agreement, the Commission operated from 2008 to 2015, gathered survivor testimony, and issued 94 Calls to Action to guide reconciliation between Indigenous peoples and other Canadians."
        ),
        Question(
            id = 158,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "The \"Quiet Revolution\" refers to a period of rapid social and political change in Quebec during the 1960s.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Under Premier Jean Lesage's government, Quebec rapidly secularized its schools and hospitals, expanded the provincial state, and asserted greater control over its economy, shifting power away from the Catholic Church under the slogan \"maitres chez nous\" (masters in our own house). (Discover Canada, page 24.)"
        ),
        Question(
            id = 159,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which Canadian Prime Minister won the Nobel Peace Prize for his role in resolving the 1956 Suez Crisis?",
            options = listOf("Wilfrid Laurier", "John A. Macdonald", "Lester B. Pearson", "R.B. Bennett"),
            correctAnswerIndex = 2,
            explanation = "Lester B. Pearson won the Nobel Peace Prize in 1957 for developing the concept of UN peacekeeping during the Suez Crisis."
        ),
        Question(
            id = 160,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "Canada has never contributed peacekeeping troops to United Nations missions.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Canada has a long history of contributing to UN peacekeeping missions around the world, beginning with the Suez Crisis in 1956."
        ),
        Question(
            id = 161,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Terry Fox is remembered in Canadian history for which achievement?",
            options = listOf(
                "His cross-Canada \"Marathon of Hope\" run to raise money for cancer research",
                "Leading the North-West Resistance",
                "Founding the city of Vancouver",
                "Negotiating Confederation"
            ),
            correctAnswerIndex = 0,
            explanation = "Terry Fox undertook his Marathon of Hope in 1980, running partway across Canada to raise money and awareness for cancer research. (Discover Canada, page 26.)"
        ),
        Question(
            id = 162,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "The October Crisis of 1970 involved the federal government invoking the War Measures Act.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "During the October Crisis of 1970, the federal government invoked the War Measures Act in response to kidnappings by the Front de libération du Québec (FLQ)."
        ),
        Question(
            id = 163,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What document ended the Seven Years' War and confirmed British control over New France?",
            options = listOf(
                "The Constitution Act, 1867",
                "The Treaty of Versailles",
                "The Statute of Westminster",
                "The Treaty of Paris (1763)"
            ),
            correctAnswerIndex = 3,
            explanation = "Signed in 1763 to end the Seven Years' War, the treaty saw France cede nearly all of New France to Britain, bringing French colonial rule in mainland Canada to a close.",
            topicGroupId = "orig_dup_group_3"
        ),
        Question(
            id = 164,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "Sir Wilfrid Laurier was Canada's first Prime Minister.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Sir Wilfrid Laurier was Canada's seventh Prime Minister; Sir John A. Macdonald was the first.",
            topicGroupId = "orig_dup_group_5"
        ),
        Question(
            id = 165,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In what year did Nunavut become a separate territory, carved out of the Northwest Territories?",
            options = listOf("2010", "1949", "1867", "1999"),
            correctAnswerIndex = 3,
            explanation = "Nunavut was created as a separate territory in 1999, following a land claims agreement with the Inuit. (Discover Canada, page 51.)"
        ),
        Question(
            id = 166,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "Canada's federal income tax was introduced in the 1800s, long before the First World War.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Federal income tax was introduced in 1917 as a temporary measure to help fund the First World War, not in the 1800s."
        ),
        Question(
            id = 167,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "The Hudson's Bay Company was historically significant in Canada primarily for its role in what industry?",
            options = listOf("Textile production", "Steel manufacturing", "Shipbuilding", "The fur trade"),
            correctAnswerIndex = 3,
            explanation = "The Hudson's Bay Company, founded in 1670, was a dominant force in the North American fur trade for centuries. (Discover Canada, page 16.)"
        ),
        Question(
            id = 168,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "Rupert's Land was transferred to Canada in 1905, the same year Alberta and Saskatchewan became provinces.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Rupert's Land was transferred to Canada in 1870, decades before Alberta and Saskatchewan became provinces in 1905."
        ),
        Question(
            id = 169,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of these best describes Canada's involvement in the First World War?",
            options = listOf(
                "Canada fought exclusively in the Pacific theatre",
                "Canada remained entirely neutral",
                "Canada only provided financial aid, with no troops",
                "Canada fought as part of the British Empire's forces and made major contributions on the Western Front"
            ),
            correctAnswerIndex = 3,
            explanation = "Canada, as part of the British Empire, sent hundreds of thousands of troops and made major contributions to the Allied war effort in World War I. (Discover Canada, page 21.)",
            topicGroupId = "orig_dup_group_30"
        ),
        Question(
            id = 170,
            category = Category.HISTORY,
            type = QuestionType.TRUE_FALSE,
            text = "Canada fought on the side of the Central Powers during the First World War.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "As part of the British Empire, Canada automatically entered the First World War in 1914 on the Allied side, fighting alongside Britain and France against Germany and the other Central Powers.",
            topicGroupId = "orig_dup_group_30"
        ),
        Question(
            id = 171,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the name of the 1982 act that gave Canada full legal authority over its own Constitution?",
            options = listOf(
                "The Statute of Westminster",
                "The Treaty of Paris",
                "The Constitution Act, 1982",
                "The Balfour Declaration"
            ),
            correctAnswerIndex = 2,
            explanation = "The Constitution Act, 1982 \"patriated\" Canada's Constitution, giving Canada full authority to amend it without approval from Britain.",
            topicGroupId = "orig_dup_group_6"
        ),

        // --- How Canadians Govern ---
        Question(
            id = 22,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who is Canada's current Governor General?",
            options = listOf("Mary Simon", "Julie Payette", "David Johnston", "Louise Arbour"),
            correctAnswerIndex = 3,
            explanation = "Louise Arbour was installed as Canada's 31st Governor General on June 8, 2026, succeeding Mary Simon."
        ),
        Question(
            id = 23,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who is Canada's current Prime Minister?",
            options = listOf("Justin Trudeau", "Pierre Poilievre", "Mark Carney", "Chrystia Freeland"),
            correctAnswerIndex = 2,
            explanation = "Mark Carney became Canada's 24th Prime Minister in March 2025."
        ),
        Question(
            id = 24,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Canada is best described as a constitutional monarchy and a...",
            options = listOf(
                "One-party state",
                "Direct democracy",
                "Parliamentary democracy",
                "Absolute monarchy"
            ),
            correctAnswerIndex = 2,
            explanation = "Canada is a constitutional monarchy and a parliamentary democracy, with an elected Parliament.",
            topicGroupId = "orig_dup_group_29"
        ),
        Question(
            id = 25,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "Canada's Head of State is the reigning King or Queen, represented in Canada by the Governor General.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The Governor General is appointed by the monarch on the advice of the Prime Minister and carries out the Crown's constitutional and ceremonial duties in Canada on the monarch's behalf. (Discover Canada, page 29.)",
            topicGroupId = "orig_dup_group_9"
        ),
        Question(
            id = 26,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "By law, federal elections in Canada must be called at least once every five years.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The Constitution requires an election at least every five years, though the fixed-date election law targets roughly every four years.",
            topicGroupId = "orig_dup_group_1"
        ),
        Question(
            id = 27,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which two chambers make up Canada's Parliament, alongside the Crown?",
            options = listOf(
                "The House of Commons and the Cabinet",
                "The Cabinet and the Supreme Court",
                "The Provincial Legislature and the Senate",
                "The House of Commons and the Senate"
            ),
            correctAnswerIndex = 3,
            explanation = "Canada's Parliament consists of the House of Commons, the Senate, and the Crown. (Discover Canada, page 28.)"
        ),
        Question(
            id = 28,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "Voting in federal elections is mandatory for Canadian citizens.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Voting is a right and a valued responsibility in Canada, but it is not legally mandatory."
        ),
        Question(
            id = 172,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How many levels of government does Canada have?",
            options = listOf(
                "Two: federal and provincial only",
                "Three: federal, provincial or territorial, and municipal",
                "Four: federal, provincial, regional, and municipal",
                "One: federal only"
            ),
            correctAnswerIndex = 1,
            explanation = "Provincial and territorial governments create municipalities and delegate local powers to them, so Canada's three levels each handle different jobs: federal for national matters, provincial/territorial for things like health and education, and municipal for local services such as roads and water. (Discover Canada, page 29.)"
        ),
        Question(
            id = 173,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "Healthcare delivery and education are mainly the responsibility of the federal government in Canada.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Healthcare delivery and education are mainly provincial responsibilities, not federal ones, though federal funding is often involved. (Discover Canada, page 24.)"
        ),
        Question(
            id = 174,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is mainly a federal government responsibility?",
            flashcardText = "What is mainly a federal government responsibility?",
            options = listOf("National defence", "Local road maintenance", "Public school curriculum", "Municipal water services"),
            correctAnswerIndex = 0,
            explanation = "National defence is a federal responsibility, while roads, schools, and local water services fall mainly to provinces and municipalities."
        ),
        Question(
            id = 175,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "The Prime Minister is directly elected by all Canadians in a separate, national vote for that specific office.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "The Prime Minister is not elected directly to that office; they are an MP who becomes Prime Minister as the leader of the party that can command the confidence of the House of Commons."
        ),
        Question(
            id = 176,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the name given to the electoral district that each Member of Parliament represents?",
            options = listOf("A ward", "A riding", "A precinct", "A territory"),
            correctAnswerIndex = 1,
            explanation = "Each riding elects one Member of Parliament to the House of Commons through a first-past-the-post vote, so the candidate with the most votes in that district wins the seat."
        ),
        Question(
            id = 177,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "Members of the Senate are elected by Canadian voters.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Senators are appointed, not elected, generally on the recommendation of the Prime Minister."
        ),
        Question(
            id = 178,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the minimum voting age in Canadian federal elections?",
            options = listOf("19", "16", "21", "18"),
            correctAnswerIndex = 3,
            explanation = "The Canada Elections Act sets 18 as the minimum voting age, so citizens become eligible to vote in the federal election held on or after their 18th birthday. (Discover Canada, page 29.)"
        ),
        Question(
            id = 179,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "Canada's Supreme Court is the highest court in the country.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The Supreme Court of Canada is the final court of appeal and the highest court in the Canadian judicial system."
        ),
        Question(
            id = 180,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who represents the Crown at the provincial level in Canada?",
            options = listOf("The Premier", "The Governor General", "The Lieutenant Governor", "The Chief Justice"),
            correctAnswerIndex = 2,
            explanation = "Each province has a Lieutenant Governor who represents the Crown provincially, similar to the Governor General's federal role. (Discover Canada, page 29.)"
        ),
        Question(
            id = 181,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "Canadian federal elections use a \"first past the post\" system, where the candidate with the most votes in a riding wins.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Canada's electoral system is \"first past the post\": the candidate with the most votes in each riding is elected, even without an absolute majority."
        ),
        Question(
            id = 182,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the political party with the second-most seats in the House of Commons commonly called?",
            options = listOf("The Minority Party", "The Official Opposition", "The Shadow Senate", "The Second Cabinet"),
            correctAnswerIndex = 1,
            explanation = "The party with the second-most seats forms the Official Opposition, which formally challenges and scrutinizes the governing party. (Discover Canada, page 31.)"
        ),
        Question(
            id = 183,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "Municipal governments are typically responsible for services such as roads, water, and local police in most areas.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Municipal governments generally deliver local services like road maintenance, water systems, and policing. (Discover Canada, page 33.)"
        ),
        Question(
            id = 184,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following best describes \"responsible government\" in the Canadian system?",
            flashcardText = "What does \"responsible government\" mean in the Canadian system?",
            options = listOf(
                "The government is chosen entirely by the Governor General",
                "The government (Cabinet) must maintain the confidence of the elected House of Commons to remain in power",
                "The government answers only to the Senate",
                "The government cannot be removed once appointed"
            ),
            correctAnswerIndex = 1,
            explanation = "Responsible government means the Cabinet must retain the confidence of the elected House of Commons to stay in office. (Discover Canada, page 18.)",
            topicGroupId = "orig_dup_group_2"
        ),
        Question(
            id = 185,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "The Governor General can refuse to grant royal assent to any bill passed by Parliament, at their own discretion, as a matter of routine.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "In modern practice, royal assent is essentially a formality, and the Governor General does not refuse it as a matter of routine."
        ),
        Question(
            id = 186,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How many judges typically sit on the Supreme Court of Canada?",
            options = listOf("Five", "Nine", "Twelve", "Seven"),
            correctAnswerIndex = 1,
            explanation = "The Supreme Court of Canada is composed of nine judges, including the Chief Justice. (Discover Canada, page 29.)"
        ),
        Question(
            id = 187,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "Immigration policy in Canada is set exclusively by provincial governments.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Immigration is a shared responsibility, with the federal government playing the leading role alongside provincial nominee programs.",
            topicGroupId = "orig_dup_group_27"
        ),
        Question(
            id = 188,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What must happen for a new bill to take effect after being passed by both the House of Commons and the Senate?",
            options = listOf(
                "It must be published in a newspaper",
                "It must be approved by the Supreme Court",
                "It must be ratified by all provinces",
                "It must receive royal assent"
            ),
            correctAnswerIndex = 3,
            explanation = "A bill becomes law once it receives royal assent, granted on behalf of the Crown. (Discover Canada, page 28.)"
        ),
        Question(
            id = 189,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "Mayors and city councillors are elected officials at the municipal level of government.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Municipalities are created by provincial governments, and their mayors and councillors are elected locally to oversee services such as roads, water, garbage collection, and policing. (Discover Canada, page 33.)"
        ),
        Question(
            id = 190,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What are the three branches of Canadian government, similar to many democracies?",
            options = listOf(
                "Cabinet, Senate, and Commons",
                "Federal, provincial, and municipal",
                "Executive, legislative, and judicial",
                "Monarchy, Parliament, and Courts"
            ),
            correctAnswerIndex = 2,
            explanation = "This separation of powers means the executive (Prime Minister and Cabinet) implements laws, the legislative branch (Parliament) makes them, and the judicial branch (the courts) interprets them, so no single branch holds unchecked power. (Discover Canada, page 29.)"
        ),
        Question(
            id = 191,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "The Cabinet is made up of the Prime Minister and Ministers responsible for government departments.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The Cabinet consists of the Prime Minister and the Ministers who lead individual government departments. (Discover Canada, page 28.)"
        ),
        Question(
            id = 192,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is a \"minority government\"?",
            options = listOf(
                "A government made up only of opposition parties",
                "A government where the ruling party holds less than half the seats in the House of Commons",
                "A government with no Prime Minister",
                "A government elected by fewer than half of eligible voters"
            ),
            correctAnswerIndex = 1,
            explanation = "A minority government occurs when the governing party does not hold a majority of seats and must rely on support from other parties to pass legislation. (Discover Canada, page 31.)"
        ),
        Question(
            id = 193,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "A federal election in Canada must be held at least once every ten years, according to the Constitution.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "The Constitution requires a federal election at least once every five years, not ten.",
            topicGroupId = "orig_dup_group_1"
        ),
        Question(
            id = 194,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the role of Elections Canada?",
            options = listOf(
                "An independent agency that administers federal elections and referendums",
                "A political party that runs in every election",
                "A branch of the Senate",
                "A private polling company"
            ),
            correctAnswerIndex = 0,
            explanation = "Elections Canada is the independent, non-partisan agency responsible for administering federal elections and referendums. (Discover Canada, page 30.)"
        ),
        Question(
            id = 195,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "A person must be a Canadian citizen to vote in a federal election.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Only Canadian citizens who are at least 18 years old are eligible to vote in federal elections. (Discover Canada, page 30.)",
            topicGroupId = "orig_dup_group_8"
        ),
        Question(
            id = 196,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the Speaker of the House of Commons responsible for?",
            options = listOf(
                "Presiding over debates and maintaining order in the House of Commons",
                "Leading the governing party",
                "Appointing Senators",
                "Representing Canada abroad"
            ),
            correctAnswerIndex = 0,
            explanation = "The Speaker presides over House of Commons debates, maintains order, and ensures rules of procedure are followed."
        ),
        Question(
            id = 197,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "The Senate is sometimes described as providing \"sober second thought\" on legislation passed by the House of Commons.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Because senators are appointed rather than elected, the Senate is meant to review and, if needed, revise or delay bills passed by the elected House of Commons before they become law."
        ),
        Question(
            id = 198,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What happens during a federal \"by-election\"?",
            options = listOf(
                "All ridings across Canada vote at once",
                "Voters in a single riding elect a new Member of Parliament outside of a general election",
                "The Prime Minister is replaced automatically",
                "The Senate is dissolved"
            ),
            correctAnswerIndex = 1,
            explanation = "A by-election is held in a single riding, typically to fill a vacancy left when an MP resigns or passes away, without triggering a general election."
        ),
        Question(
            id = 199,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "The Governor General can summon, prorogue, and dissolve Parliament on the advice of the Prime Minister.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The Governor General exercises these formal powers on the advice of the Prime Minister, as part of Canada's system of constitutional monarchy."
        ),
        Question(
            id = 200,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the purpose of \"Question Period\" in the House of Commons?",
            options = listOf(
                "It is reserved for foreign dignitaries to speak",
                "It is when new laws are automatically approved",
                "It allows opposition MPs to question the government and hold it accountable",
                "It only happens once per year"
            ),
            correctAnswerIndex = 2,
            explanation = "Question Period is a daily opportunity for opposition MPs to question government ministers and hold them accountable. (Discover Canada, page 31.)"
        ),
        Question(
            id = 201,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "Canada is best described as a constitutional monarchy and a parliamentary democracy.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Canada combines a constitutional monarchy, where the monarch's powers are limited by law, with a parliamentary democracy.",
            topicGroupId = "orig_dup_group_29"
        ),
        Question(
            id = 202,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Roughly how many Members of Parliament sit in the House of Commons?",
            options = listOf("Exactly 100", "Around 50", "Around 1,000", "Around 340"),
            correctAnswerIndex = 3,
            explanation = "The House of Commons has over 300 seats, with the exact number periodically adjusted to reflect population changes across ridings."
        ),
        Question(
            id = 203,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "The Prime Minister is Canada's Head of State.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "The reigning monarch is Canada's Head of State; the Prime Minister is the head of government, a distinct role. (Discover Canada, page 29.)",
            topicGroupId = "orig_dup_group_9"
        ),
        Question(
            id = 204,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the Privy Council Office generally understood to support?",
            options = listOf(
                "The Prime Minister and Cabinet in the daily operation of government",
                "The Supreme Court's scheduling",
                "Municipal bylaw enforcement",
                "Provincial elections"
            ),
            correctAnswerIndex = 0,
            explanation = "The Privy Council Office is the Prime Minister's own public service department, staffed by non-partisan officials and headed by the Clerk of the Privy Council, who helps coordinate Cabinet decisions and the daily business of government."
        ),
        Question(
            id = 205,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "Provincial governments have their own legislatures, separate from the federal Parliament.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Each province has its own elected legislature responsible for provincial matters, distinct from the federal Parliament in Ottawa."
        ),
        Question(
            id = 206,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What term describes the head of government at the provincial level, similar to the Prime Minister federally?",
            options = listOf("Premier", "Governor", "Chancellor", "President"),
            correctAnswerIndex = 0,
            explanation = "The Premier is the leader of the party holding the most seats in a province's or territory's legislature, exercising executive authority there much as the Prime Minister does federally. (Discover Canada, page 29.)"
        ),
        Question(
            id = 207,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "Municipal bylaws must always be approved by the federal government before taking effect.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Municipal governments have the authority to pass their own bylaws within powers granted by their province, without federal approval."
        ),
        Question(
            id = 208,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is a \"coalition government\"?",
            options = listOf(
                "Another term for the Senate",
                "A government made up of unelected officials",
                "A government formed by two or more parties agreeing to govern together",
                "A government led by the Governor General directly"
            ),
            correctAnswerIndex = 2,
            explanation = "A coalition government occurs when two or more political parties formally agree to govern together, typically to secure a majority."
        ),
        Question(
            id = 209,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "The Prime Minister presides over the Supreme Court of Canada.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "The Chief Justice of Canada, not the Prime Minister, presides over the Supreme Court. (Discover Canada, page 29.)"
        ),
        Question(
            id = 210,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is an example of a federal government responsibility, as opposed to a provincial one?",
            flashcardText = "What is an example of a federal (not provincial) government responsibility?",
            options = listOf("Highway maintenance", "Hospitals", "K-12 education curriculum", "Citizenship"),
            correctAnswerIndex = 3,
            explanation = "Citizenship is a federal responsibility (immigration is shared between federal and provincial governments), while hospitals, education, and most highways fall under provincial jurisdiction.",
            topicGroupId = "orig_dup_group_27"
        ),
        Question(
            id = 211,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "A federal government can remain in power indefinitely, even after losing a formal vote of confidence in the House of Commons.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "A government that loses a confidence vote must resign or request the dissolution of Parliament, leading to a new election. (Discover Canada, page 31.)",
            topicGroupId = "orig_dup_group_2"
        ),
        Question(
            id = 212,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How are candidates typically chosen to run for a political party in a riding?",
            options = listOf(
                "Through a nomination process within the party",
                "Appointed directly by the Governor General",
                "Selected by the Supreme Court",
                "Chosen at random by Elections Canada"
            ),
            correctAnswerIndex = 0,
            explanation = "Local riding association members vote at a nomination meeting to choose the person who will represent their party as the candidate in that riding."
        ),
        Question(
            id = 213,
            category = Category.GOVERNMENT,
            type = QuestionType.TRUE_FALSE,
            text = "Permanent residents of Canada are eligible to vote in provincial and federal elections.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Only Canadian citizens can vote in provincial and federal elections; permanent residents are not eligible. (Discover Canada, page 30.)",
            topicGroupId = "orig_dup_group_8"
        ),
        Question(
            id = 214,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does it mean that Canada is a \"federal\" state?",
            options = listOf(
                "Provinces have no elected governments",
                "All power rests solely with the federal government",
                "Power is constitutionally divided between a central (federal) government and provincial/territorial governments",
                "Only municipalities can pass laws"
            ),
            correctAnswerIndex = 2,
            explanation = "Federalism means governing power is constitutionally divided between the federal government and the provinces and territories. (Discover Canada, page 28.)"
        ),

        // --- Canadian Symbols ---
        Question(
            id = 29,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What symbol is featured at the centre of the Canadian flag?",
            options = listOf("An eagle", "A maple leaf", "A beaver", "A crown"),
            correctAnswerIndex = 1,
            explanation = "The eleven-point red maple leaf became the centrepiece of the flag adopted in 1965; red and white were proclaimed Canada's official colours by King George V in 1921."
        ),
        Question(
            id = 30,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "The current Canadian flag was adopted in 1965.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The maple leaf flag was officially adopted on February 15, 1965."
        ),
        Question(
            id = 31,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the title of Canada's national anthem?",
            options = listOf("God Save the King", "O Canada", "The Maple Leaf Forever", "True North"),
            correctAnswerIndex = 1,
            explanation = "Written in 1880 with music by Calixa Lavallee and French lyrics by Adolphe-Basile Routhier, \"O Canada\" was sung for decades before Parliament made it the official national anthem in 1980. (Discover Canada, page 40.)"
        ),
        Question(
            id = 32,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "The beaver is an official symbol of Canada.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The beaver was given official status as a symbol of Canada by an Act of Parliament in 1975."
        ),
        Question(
            id = 33,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What bird appears on the Canadian one-dollar coin, giving it its nickname?",
            options = listOf("A goose", "An eagle", "A loon", "A cardinal"),
            correctAnswerIndex = 2,
            explanation = "The one-dollar coin features a loon, which is why it's nicknamed the \"loonie.\""
        ),
        Question(
            id = 34,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "Canada's Latin motto \"A Mari Usque Ad Mare\" means \"From Sea to Sea.\"",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Adopted along with the Arms of Canada in 1921, the motto comes from Psalm 72 in the Bible and reflects the country's expanse between the Atlantic and Pacific oceans. (Discover Canada, page 38.)",
            topicGroupId = "orig_dup_group_28"
        ),
        Question(
            id = 35,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What two colours appear on the Canadian flag?",
            options = listOf("Blue and white", "Red and white", "Red and gold", "Green and white"),
            correctAnswerIndex = 1,
            explanation = "The Canadian flag is red and white, the official colours of Canada proclaimed by King George V in 1921. (Discover Canada, page 38.)",
            topicGroupId = "orig_dup_group_32"
        ),
        Question(
            id = 215,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the name of Canada's official coat of arms?",
            options = listOf("The National Crest", "The Great Shield", "The Arms of Canada", "The Dominion Seal"),
            correctAnswerIndex = 2,
            explanation = "Granted by King George V in 1921, the Arms of Canada combine the royal symbols of England, Scotland, Ireland, and France, the country's four founding peoples, along with a sprig of maple leaves."
        ),
        Question(
            id = 216,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "The oak tree is Canada's official national tree.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "The maple tree, not the oak, is Canada's official national tree, reflected in the maple leaf on the flag."
        ),
        Question(
            id = 217,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "On what date is Canada Day celebrated?",
            options = listOf("July 1", "June 24", "September 1", "November 11"),
            correctAnswerIndex = 0,
            explanation = "Canada Day is celebrated on July 1, marking the anniversary of Confederation in 1867."
        ),
        Question(
            id = 218,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "Remembrance Day, observed on November 11, honours Canadians who served and died in war.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "November 11 marks the anniversary of the Armistice that ended the First World War at 11 a.m. on the 11th day of the 11th month in 1918. (Discover Canada, page 21.)"
        ),
        Question(
            id = 219,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which flower/symbol is traditionally worn in Canada around Remembrance Day?",
            options = listOf("The rose", "The tulip", "The poppy", "The maple blossom"),
            correctAnswerIndex = 2,
            explanation = "The poppy became a symbol of remembrance after Canadian military doctor John McCrae's 1915 poem \"In Flanders Fields\" described the flowers growing over soldiers' graves on the battlefields of Belgium."
        ),
        Question(
            id = 220,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "Before 1965, Canada's official flag already featured the red maple leaf design used today.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Before 1965, Canada's official flag was the Red Ensign, which featured the Union Jack; the maple leaf design was adopted in 1965."
        ),
        Question(
            id = 221,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the highest honour a Canadian civilian can receive, recognizing outstanding achievement and service?",
            options = listOf("The Order of Canada", "The Victoria Cross", "The Golden Maple Award", "The Confederation Medal"),
            correctAnswerIndex = 0,
            explanation = "Established in 1967 to mark Canada's centennial, the Order of Canada is awarded by the Governor General in three grades - Companion, Officer, and Member - for outstanding lifetime achievement and service. (Discover Canada, page 40.)"
        ),
        Question(
            id = 222,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "The RCMP (Royal Canadian Mounted Police) is recognized as a national symbol of Canada.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The Royal Canadian Mounted Police, with its distinctive red serge uniform, is widely recognized as a national symbol. (Discover Canada, page 19.)"
        ),
        Question(
            id = 223,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What two colours were proclaimed as Canada's official colours by King George V in 1921?",
            options = listOf("Red and gold", "Blue and gold", "Green and white", "Red and white"),
            correctAnswerIndex = 3,
            explanation = "In 1921, King George V's royal proclamation that granted Canada its Coat of Arms also formally designated red and white as the country's official colours - the same colours adopted for the national flag in 1965. (Discover Canada, page 38.)",
            topicGroupId = "orig_dup_group_32"
        ),
        Question(
            id = 224,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "The Canadian Horse has never been officially recognized as a national symbol.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "The Canadian Horse was declared Canada's official national horse breed by an Act of Parliament in 2002."
        ),
        Question(
            id = 225,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Where does the Peace Tower, a well-known Canadian landmark, stand?",
            options = listOf("In Toronto's downtown core", "On Parliament Hill in Ottawa", "In Quebec City", "In Vancouver"),
            correctAnswerIndex = 1,
            explanation = "Completed in 1927, the Peace Tower replaced the original Victoria Tower, which was destroyed in the 1916 Parliament Hill fire. It stands on Parliament Hill in Ottawa and houses the Memorial Chamber honouring Canada's war dead."
        ),
        Question(
            id = 226,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "Victoria Day and Canada Day fall on the same calendar date each year.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Victoria Day is celebrated in May, while Canada Day is celebrated on July 1 — they fall on different dates."
        ),
        Question(
            id = 227,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does the Vimy Ridge memorial in France commemorate?",
            options = listOf(
                "Canadian soldiers who fought and died in the First World War",
                "The signing of Confederation",
                "The founding of New France",
                "The Klondike Gold Rush"
            ),
            correctAnswerIndex = 0,
            explanation = "The Vimy Ridge memorial honours the Canadian soldiers who died in the April 1917 battle, when all four divisions of the Canadian Corps fought together for the first time - a victory often described as a defining moment in Canada's emergence as a nation. (Discover Canada, page 21.)"
        ),
        Question(
            id = 228,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "Canada's coat of arms includes the motto \"A Mari Usque Ad Mare.\"",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "\"A Mari Usque Ad Mare,\" meaning \"From Sea to Sea,\" appears on the Arms of Canada. (Discover Canada, page 38.)"
        ),
        Question(
            id = 229,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which animal is featured on the Canadian five-cent coin (the \"nickel\")?",
            options = listOf("The moose", "The beaver", "The bear", "The wolf"),
            correctAnswerIndex = 1,
            explanation = "The beaver has appeared on the Canadian five-cent coin since 1937 and is a recognized national symbol.",
            topicGroupId = "orig_dup_group_12"
        ),
        Question(
            id = 230,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "Thanksgiving in Canada is celebrated on the same date as Thanksgiving in the United States.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Canadian Thanksgiving falls on the second Monday of October, while American Thanksgiving is celebrated on the fourth Thursday of November."
        ),
        Question(
            id = 231,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which sport is legally recognized as Canada's official winter sport?",
            options = listOf("Basketball", "Ice hockey", "Curling", "Skiing"),
            correctAnswerIndex = 1,
            explanation = "Under the National Sports of Canada Act (1994), ice hockey is recognized as Canada's official winter sport. (Discover Canada, page 39.)"
        ),
        Question(
            id = 232,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "Ice hockey, not lacrosse, is legally recognized as Canada's official summer sport.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Under the National Sports of Canada Act (1994), lacrosse - a game with Indigenous origins - was designated Canada's official summer sport, while ice hockey was designated the official winter sport."
        ),
        Question(
            id = 233,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How many points does the stylized maple leaf on the Canadian flag have?",
            options = listOf("8", "5", "11", "13"),
            correctAnswerIndex = 2,
            explanation = "The flag's stylized 11-point maple leaf, adopted with the 1965 National Flag of Canada, was designed to be simple and symmetrical so it reads clearly as a single bold shape even at a distance or in windy conditions."
        ),
        Question(
            id = 234,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "The Canadian flag's design uses a proportion of red-white-red panels of equal width.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The flag's design consists of two red bars and a central white square, each occupying equal-width panels in a 1:2:1 ratio."
        ),
        Question(
            id = 235,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In what year was \"O Canada\" officially proclaimed as Canada's national anthem?",
            options = listOf("1965", "1867", "1921", "1980"),
            correctAnswerIndex = 3,
            explanation = "\"O Canada\" was officially proclaimed as Canada's national anthem in 1980, though the music dates back to 1880."
        ),
        Question(
            id = 236,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "The music for \"O Canada\" was composed before the English lyrics most commonly sung today were written.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The music was composed by Calixa Lavallée in 1880, while the widely used English lyrics were written later, by Robert Stanley Weir in 1908."
        ),
        Question(
            id = 237,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "The Canadian two-dollar coin is commonly nicknamed what?",
            options = listOf("The loonie", "The toonie", "The double loon", "The polar bear coin"),
            correctAnswerIndex = 1,
            explanation = "The two-dollar coin is nicknamed the \"toonie,\" a play on \"two\" and \"loonie.\""
        ),
        Question(
            id = 238,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "The current Canadian flag is sometimes referred to as the \"Union Jack Flag.\"",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "The current flag is informally called the \"Maple Leaf Flag\" because of its central red maple leaf design, adopted in 1965."
        ),
        Question(
            id = 239,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the Canadian Forces' official aerobatic demonstration team called?",
            options = listOf("The Snowbirds", "The Maple Flyers", "The Red Arrows", "The Northern Eagles"),
            correctAnswerIndex = 0,
            explanation = "Formally known as 431 Air Demonstration Squadron, the Snowbirds have served as the Canadian Forces' official aerobatic team since 1971 and are based at 15 Wing in Moose Jaw, Saskatchewan."
        ),
        Question(
            id = 240,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "Parliament Hill, in Ottawa, is home to Canada's Parliament buildings.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Parliament Hill in Ottawa is the site of Canada's Parliament buildings, including the Centre Block and Peace Tower."
        ),
        Question(
            id = 241,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "The fleur-de-lys is closely associated with the symbols and flag of which Canadian province?",
            options = listOf("Nova Scotia", "Alberta", "Quebec", "British Columbia"),
            correctAnswerIndex = 2,
            explanation = "The fleur-de-lys, reflecting Quebec's French heritage, appears on the provincial flag of Quebec."
        ),
        Question(
            id = 242,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "Canadian territories do not have their own official flags, only provinces do.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Like each province, every Canadian territory - Yukon, the Northwest Territories, and Nunavut - has adopted its own official flag bearing symbols that reflect its distinct regional and cultural identity."
        ),
        Question(
            id = 243,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does the Crown symbolize in the context of Canadian government and identity?",
            options = listOf(
                "A specific political party",
                "The continuity of the state and Canada's constitutional monarchy",
                "The Supreme Court's authority",
                "Only Canada's colonial past, with no ongoing role"
            ),
            correctAnswerIndex = 1,
            explanation = "As Canada's head of state, the Crown is the legal source of executive authority under the Constitution and represents the continuity of the state across changes in government and prime ministers, reflecting Canada's status as a constitutional monarchy."
        ),
        Question(
            id = 244,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "The Great Seal of Canada is used to formally authenticate certain official government documents.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The Great Seal of Canada is affixed to certain official documents, such as proclamations, to authenticate them on behalf of the Crown."
        ),
        Question(
            id = 245,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which Canadian symbol is closely associated with the fur trade and appears on the five-cent coin?",
            options = listOf("The lynx", "The wolf", "The moose", "The beaver"),
            correctAnswerIndex = 3,
            explanation = "The beaver became a national symbol because its pelt drove the fur trade that funded much of early European exploration and settlement in Canada; it was formally adopted as a symbol of Canada in 1975 and has appeared on the five-cent coin since 1937. (Discover Canada, page 39.)",
            topicGroupId = "orig_dup_group_12"
        ),
        Question(
            id = 246,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "Boxing Day, on December 26, is not observed as a holiday anywhere in Canada.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Boxing Day is in fact a recognized holiday in Canada, often associated with shopping sales and family time. (Discover Canada, page 41.)"
        ),
        Question(
            id = 247,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is a well-known Canadian national symbol that features the maple leaf?",
            flashcardText = "What well-known Canadian national symbol features the maple leaf?",
            options = listOf("A palm tree", "A cactus", "The Canadian flag", "A desert scene"),
            correctAnswerIndex = 2,
            explanation = "Adopted in 1965 after a lengthy national debate over Canada's flag design, the red-and-white maple leaf flag is the country's most recognizable emblem and is frequently paired with the maple leaf motif in official branding and national celebrations. (Discover Canada, page 38.)"
        ),
        Question(
            id = 248,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "The national anthem \"O Canada\" was originally written and first performed in French.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "\"O Canada\" was originally composed with French lyrics and first performed in Quebec City in 1880. (Discover Canada, page 40.)"
        ),
        Question(
            id = 249,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "The Tomb of the Unknown Soldier, a national war memorial site, is located where?",
            options = listOf(
                "At the National War Memorial in Ottawa",
                "In Toronto's city hall",
                "At Vimy Ridge in France",
                "In Halifax harbour"
            ),
            correctAnswerIndex = 0,
            explanation = "Added to the National War Memorial in Ottawa in 2000, the Tomb of the Unknown Soldier holds the remains of an unidentified Canadian soldier who died near Vimy Ridge during the First World War."
        ),
        Question(
            id = 250,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "Canada's Coat of Arms includes images representing England, Scotland, Ireland, and France, reflecting its founding heritage.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Granted by royal proclamation in 1921, the Arms of Canada combine the three lions of England, the lion rampant of Scotland, the harp of Ireland, and the fleurs-de-lis of France - heraldic symbols of the four nations most associated with Canada's founding European heritage."
        ),
        Question(
            id = 251,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is Labour Day, observed on the first Monday of September, meant to recognize?",
            options = listOf(
                "The founding of Confederation",
                "The contributions and achievements of workers",
                "The end of the fur trade",
                "The signing of the Charter"
            ),
            correctAnswerIndex = 1,
            explanation = "Labour Day recognizes the social and economic contributions of workers and the labour movement. (Discover Canada, page 41.)"
        ),
        Question(
            id = 252,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "The maple leaf did not appear as a Canadian symbol until the 1965 flag was adopted.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "The maple leaf was used as an emblem of Canada well before 1965, appearing on coins, coats of arms, and other symbols throughout the 19th and 20th centuries. (Discover Canada, page 38.)"
        ),
        Question(
            id = 253,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does the phrase \"From Sea to Sea\" (A Mari Usque Ad Mare) reflect about Canada?",
            options = listOf(
                "Canada's vast geography, spanning from the Atlantic to the Pacific Ocean",
                "Canada's naval history only",
                "A reference to the Great Lakes only",
                "A slogan created for the 1965 flag"
            ),
            correctAnswerIndex = 0,
            explanation = "Canada's motto, taken from Psalm 72:8 (\"He shall have dominion also from sea to sea\"), was adopted alongside the Arms of Canada in 1921 and reflects the country's vast geography stretching between the Atlantic and Pacific oceans. (Discover Canada, page 38.)",
            topicGroupId = "orig_dup_group_28"
        ),
        Question(
            id = 254,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "The RCMP's iconic ceremonial uniform is coloured blue, not red.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "The RCMP's iconic ceremonial uniform is red, known as the \"Red Serge,\" not blue."
        ),
        Question(
            id = 255,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following best describes the significance of the Confederation Medal?",
            flashcardText = "What is the significance of the Confederation Medal?",
            options = listOf(
                "It replaced the Canadian flag in 1967",
                "It is awarded only to Prime Ministers",
                "It was created to commemorate the 125th anniversary of Confederation",
                "It is given to newly naturalized citizens automatically"
            ),
            correctAnswerIndex = 2,
            explanation = "The Confederation Medal was created to commemorate the 125th anniversary of Canadian Confederation in 1992."
        ),
        Question(
            id = 256,
            category = Category.SYMBOLS,
            type = QuestionType.TRUE_FALSE,
            text = "Family Day is observed as a statutory holiday in every Canadian province and territory on the same date.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Family Day is a holiday in several provinces, but not all, and the date can vary depending on the province."
        ),
        Question(
            id = 257,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of these is an official emblem recognized on Canada's coat of arms, representing Ireland?",
            options = listOf("A rose", "A thistle", "A lily", "A shamrock"),
            correctAnswerIndex = 3,
            explanation = "A shamrock appears on the Arms of Canada as one of the heraldic symbols representing Ireland's contribution to Canada's founding heritage."
        ),

        // --- Economy & Geography ---
        Question(
            id = 36,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How many provinces and territories make up Canada?",
            options = listOf(
                "12 provinces only",
                "10 provinces and 3 territories",
                "10 provinces and 2 territories",
                "13 provinces"
            ),
            correctAnswerIndex = 1,
            explanation = "Canada's 10 provinces derive their powers directly from the Constitution Act, 1867, while its 3 territories - Yukon, the Northwest Territories, and Nunavut - exercise powers delegated to them by the federal Parliament, which is why the two categories are counted separately."
        ),
        Question(
            id = 37,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "Canada has coastlines on the Atlantic, Pacific, and Arctic Oceans.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Canada borders three oceans: the Atlantic, the Pacific, and the Arctic, giving it the longest coastline of any country.",
            topicGroupId = "orig_dup_group_24"
        ),
        Question(
            id = 38,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the capital city of Canada?",
            options = listOf("Toronto", "Vancouver", "Ottawa", "Montreal"),
            correctAnswerIndex = 2,
            explanation = "Ottawa, in Ontario, is the capital city of Canada."
        ),
        Question(
            id = 39,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "Canada is the second-largest country in the world by total land area.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Canada is the second-largest country in the world by area, after Russia. (Discover Canada, page 44.)",
            topicGroupId = "orig_dup_group_7"
        ),
        Question(
            id = 40,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is one of Canada's three territories?",
            flashcardText = "What is one of Canada's three territories?",
            options = listOf("Alberta", "Nunavut", "Manitoba", "Newfoundland and Labrador"),
            correctAnswerIndex = 1,
            explanation = "Nunavut, along with Yukon and the Northwest Territories, is one of Canada's three territories."
        ),
        Question(
            id = 41,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "The Canadian one-dollar coin is nicknamed the \"loonie.\"",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "It's nicknamed the \"loonie\" after the loon pictured on it."
        ),
        Question(
            id = 42,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which industries have historically been especially significant to Canada's economy?",
            options = listOf(
                "Only software and technology",
                "Only tourism",
                "Fishing, forestry, mining, and energy",
                "Only manufacturing"
            ),
            correctAnswerIndex = 2,
            explanation = "Canada's economy grew historically around natural resources - furs, Atlantic and Pacific fisheries, timber, minerals, and later oil and gas - a pattern often called a \"staples economy\" that shaped early settlement and trade patterns.",
            topicGroupId = "orig_dup_group_21"
        ),
        Question(
            id = 258,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the capital city of British Columbia?",
            options = listOf("Surrey", "Vancouver", "Kelowna", "Victoria"),
            correctAnswerIndex = 3,
            explanation = "Victoria, not Vancouver, is the capital city of British Columbia."
        ),
        Question(
            id = 259,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "Toronto is the capital of Ontario.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Toronto is both Ontario's capital and Canada's most populous city."
        ),
        Question(
            id = 260,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the capital of Quebec?",
            options = listOf("Gatineau", "Montreal", "Laval", "Quebec City"),
            correctAnswerIndex = 3,
            explanation = "Quebec City is the capital of Quebec, though Montreal is the province's largest city."
        ),
        Question(
            id = 261,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "Edmonton is the capital of Alberta.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Edmonton is Alberta's capital, while Calgary is the province's largest city."
        ),
        Question(
            id = 262,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which body of water forms part of the border between Canada and the United States, including Lake Superior and Lake Ontario?",
            options = listOf("The Gulf of St. Lawrence", "Hudson Bay", "The Great Lakes", "The Beaufort Sea"),
            correctAnswerIndex = 2,
            explanation = "Four of the five Great Lakes - Superior, Huron, Erie, and Ontario - are shared between Canada and the United States and form part of the border, while Lake Michigan lies entirely within the U.S."
        ),
        Question(
            id = 263,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "Canada shares the longest international land border in the world with the United States.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Stretching roughly 8,900 km, including the Alaska-Yukon/B.C. section, the Canada-U.S. border is the longest international land border in the world and is often called the \"longest undefended border.\". (Discover Canada, page 43.)"
        ),
        Question(
            id = 264,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the name of Canada's longest river?",
            options = listOf("The Fraser River", "The Mackenzie River", "The Ottawa River", "The Saskatchewan River"),
            correctAnswerIndex = 1,
            explanation = "The Mackenzie River, flowing through the Northwest Territories, is Canada's longest river. (Discover Canada, page 50.)"
        ),
        Question(
            id = 265,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "The Canadian Shield is a mountain range located primarily in British Columbia.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "The Canadian Shield is a vast area of ancient rock covering much of central and eastern Canada, not a mountain range in British Columbia."
        ),
        Question(
            id = 266,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the official currency of Canada?",
            options = listOf("The Canadian pound", "The Canadian dollar", "The Canadian franc", "The Canadian mark"),
            correctAnswerIndex = 1,
            explanation = "The Canadian dollar, informally nicknamed the \"loonie\" after the loon depicted on the one-dollar coin, replaced earlier colonial currencies when Canada adopted a decimal currency system in the mid-19th century."
        ),
        Question(
            id = 267,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "The United States is Canada's largest trading partner.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The United States has long been Canada's largest trading partner by a wide margin. (Discover Canada, page 43.)"
        ),
        Question(
            id = 268,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the capital of Nova Scotia?",
            options = listOf("Dartmouth", "Sydney", "Halifax", "Truro"),
            correctAnswerIndex = 2,
            explanation = "Halifax is the capital of Nova Scotia and a major Atlantic port city."
        ),
        Question(
            id = 269,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "Nunavut's capital is Yellowknife.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Nunavut's capital is Iqaluit; Yellowknife is the capital of the Northwest Territories."
        ),
        Question(
            id = 270,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "According to Discover Canada, Canada is generally divided into how many main regions?",
            options = listOf("Five", "Two", "Ten", "Three"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada describes five main regions: the Atlantic Provinces, Central Canada, the Prairie Provinces, the West Coast, and the North."
        ),
        Question(
            id = 271,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "Winnipeg is the capital of Saskatchewan.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Winnipeg is the capital of Manitoba; Saskatchewan's capital is Regina."
        ),
        Question(
            id = 272,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which natural resource sectors have historically driven much of Canada's export economy?",
            options = listOf(
                "Only tourism",
                "Only fashion and retail",
                "Only banking",
                "Energy, minerals, and forestry"
            ),
            correctAnswerIndex = 3,
            explanation = "Canada is a major global exporter of natural resources, including oil and gas from Alberta's energy sector, minerals such as potash and nickel, and forestry products like softwood lumber and pulp. (Discover Canada, page 42.)",
            topicGroupId = "orig_dup_group_21"
        ),
        Question(
            id = 273,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "Canada's climate is uniform across the entire country, with little regional variation.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Canada's climate varies greatly by region, from mild coastal areas to harsh Arctic conditions in the North."
        ),
        Question(
            id = 274,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the capital of the Yukon?",
            options = listOf("Whitehorse", "Dawson City", "Watson Lake", "Carmacks"),
            correctAnswerIndex = 0,
            explanation = "Whitehorse is the capital of the Yukon and its largest community."
        ),
        Question(
            id = 275,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "St. John's is the capital of Newfoundland and Labrador.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "St. John's is the capital and largest city of Newfoundland and Labrador."
        ),
        Question(
            id = 276,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the capital of New Brunswick?",
            options = listOf("Fredericton", "Saint John", "Moncton", "Bathurst"),
            correctAnswerIndex = 0,
            explanation = "Fredericton is the capital of New Brunswick, while Saint John and Moncton are larger cities in the province."
        ),
        Question(
            id = 277,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "Summerside, not Charlottetown, is the capital of Prince Edward Island.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Charlottetown, not Summerside, is the capital of Prince Edward Island, and was the site of the 1864 Charlottetown Conference leading to Confederation."
        ),
        Question(
            id = 278,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How many time zones does Canada span, from coast to coast?",
            options = listOf("Ten", "One", "Three", "Six"),
            correctAnswerIndex = 3,
            explanation = "Canada spans six time zones, from Newfoundland Time in the east to Pacific Time in the west."
        ),
        Question(
            id = 279,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "Great Bear Lake, located in the Northwest Territories, is the largest lake located entirely within Canada.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Great Bear Lake is the largest lake located entirely within Canada (the Great Lakes are shared with the United States)."
        ),
        Question(
            id = 280,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the name of Canada's highest mountain peak?",
            options = listOf("Mount Logan", "Mount Robson", "Mount Rundle", "Mount Assiniboine"),
            correctAnswerIndex = 0,
            explanation = "Mount Logan, located in Yukon, is Canada's highest peak."
        ),
        Question(
            id = 281,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "The Rocky Mountains are located primarily in Ontario and Quebec.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "The Rocky Mountains extend through western Alberta and eastern British Columbia, not Ontario and Quebec."
        ),
        Question(
            id = 282,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "The Prairie provinces are especially known for which agricultural product?",
            options = listOf("Coffee", "Wheat and canola", "Rice", "Bananas"),
            correctAnswerIndex = 1,
            explanation = "The Prairie provinces (Manitoba, Saskatchewan, Alberta) are major producers of wheat and canola."
        ),
        Question(
            id = 283,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "The Grand Banks, off the coast of Newfoundland, were historically one of the world's richest fishing grounds.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The Grand Banks off Newfoundland were historically among the richest fishing grounds in the world, especially for cod."
        ),
        Question(
            id = 284,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the name of the famous waterfall located on the border between Ontario and New York State?",
            options = listOf("Horseshoe Falls only", "Montmorency Falls", "Athabasca Falls", "Niagara Falls"),
            correctAnswerIndex = 3,
            explanation = "Niagara Falls, on the border between Ontario and New York State, is one of Canada's best-known natural landmarks."
        ),
        Question(
            id = 285,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "The St. Lawrence Seaway allows ocean-going ships to travel deep into the interior of North America.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "The St. Lawrence Seaway is a system of locks and canals allowing ocean-going vessels to reach the Great Lakes."
        ),
        Question(
            id = 286,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the name of the highway that stretches across Canada, connecting the country from coast to coast?",
            options = listOf("The Yellowhead Route only", "The Alaska Highway", "Highway 401 only", "The Trans-Canada Highway"),
            correctAnswerIndex = 3,
            explanation = "Officially completed in 1962, the Trans-Canada Highway runs roughly 8,000 km from St. John's, Newfoundland, to Victoria, British Columbia, making it one of the longest national highways in the world."
        ),
        Question(
            id = 287,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "Prince Edward Island is Canada's largest producer of oil and natural gas.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Alberta, not Prince Edward Island, is Canada's largest producer of oil and natural gas."
        ),
        Question(
            id = 288,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is Canada's most populous city?",
            flashcardText = "What is Canada's most populous city?",
            options = listOf("Toronto", "Montreal", "Vancouver", "Ottawa"),
            correctAnswerIndex = 0,
            explanation = "Toronto is Canada's most populous city and a major economic hub. (Discover Canada, page 48.)"
        ),
        Question(
            id = 289,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "Hydroelectric power plays no significant role in Canada's electricity generation.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Hydroelectric power is a major source of electricity in Canada, especially in provinces like Quebec and British Columbia."
        ),
        Question(
            id = 290,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which two provinces are generally considered Canada's manufacturing heartland, especially for the automotive industry?",
            options = listOf("Nova Scotia and PEI", "Alberta and Saskatchewan", "British Columbia and Yukon", "Ontario and Quebec"),
            correctAnswerIndex = 3,
            explanation = "Ontario and Quebec form Canada's industrial heartland, benefiting from dense population, the St. Lawrence Seaway, and proximity to major U.S. markets; Ontario in particular has long been closely linked to the U.S. Midwest's automotive manufacturing industry. (Discover Canada, page 47.)"
        ),
        Question(
            id = 291,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "Canada's currency is divided into dollars and cents, with 100 cents in one dollar.",
            options = listOf("True", "False"),
            correctAnswerIndex = 0,
            explanation = "Canada moved to a decimal currency system in the 19th century, so like the U.S. dollar, the Canadian dollar is divided into 100 cents rather than the shillings-and-pence system used earlier under British colonial currency."
        ),
        Question(
            id = 292,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following best describes Canada's north in terms of population?",
            flashcardText = "How would you describe Canada's north in terms of population?",
            options = listOf(
                "It has a small population spread over a vast area",
                "It is the most densely populated part of Canada",
                "It has no permanent residents",
                "It is entirely uninhabited"
            ),
            correctAnswerIndex = 0,
            explanation = "Canada's three northern territories cover roughly a third of the country's landmass but are home to only around 100,000 people combined, a reflection of the region's harsh climate, permafrost, and remoteness; a substantial share of northern residents are Indigenous, including many Inuit."
        ),
        Question(
            id = 293,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "Forestry has historically played almost no role in British Columbia's economy.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "British Columbia's extensive temperate rainforests have long made forestry one of the province's most important industries, and B.C. remains one of Canada's largest producers of lumber and wood products."
        ),
        Question(
            id = 294,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which body of water lies to the east of Quebec's Gaspé Peninsula and is a major shipping route?",
            options = listOf("Lake Winnipeg", "Hudson Bay", "The Beaufort Sea", "The Gulf of St. Lawrence"),
            correctAnswerIndex = 3,
            explanation = "The Gulf of St. Lawrence lies east of the Gaspé Peninsula and connects the St. Lawrence River to the Atlantic Ocean."
        ),
        Question(
            id = 295,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "Winnipeg has historically had little importance as a transportation or rail hub.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Winnipeg has in fact long served as a major transportation and rail hub, due to its central location in Canada."
        ),
        Question(
            id = 296,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is a major economic activity in Canada's Atlantic provinces, tied closely to the ocean?",
            options = listOf("Fishing", "Wheat farming", "Oil sands extraction", "Wine production only"),
            correctAnswerIndex = 0,
            explanation = "Fishing has long been a major economic activity in Canada's Atlantic provinces, given their extensive coastlines."
        ),
        Question(
            id = 297,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "Canada is entirely landlocked and has no ocean coastline.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Canada has the longest coastline of any country in the world, bordering the Atlantic, Pacific, and Arctic Oceans.",
            topicGroupId = "orig_dup_group_24"
        ),
        Question(
            id = 298,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which Canadian city is well known as a centre for the technology sector, sometimes nicknamed \"Silicon Valley North\"?",
            options = listOf("Toronto/Waterloo region", "Charlottetown", "Regina", "Yellowknife"),
            correctAnswerIndex = 0,
            explanation = "The Toronto-Waterloo corridor in Ontario has grown into a major technology hub, sometimes compared to California's Silicon Valley."
        ),
        Question(
            id = 299,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.TRUE_FALSE,
            text = "Canada is the largest country in the world by total land area.",
            options = listOf("True", "False"),
            correctAnswerIndex = 1,
            explanation = "Canada is the second-largest country in the world by area, after Russia.",
            topicGroupId = "orig_dup_group_7"
        ),
        Question(
            id = 300,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What are Canada's three coastal oceans?",
            options = listOf(
                "The Atlantic, Pacific, and Arctic Oceans",
                "The Atlantic, Indian, and Pacific Oceans",
                "The Pacific and Atlantic Oceans only",
                "The Arctic Ocean only"
            ),
            correctAnswerIndex = 0,
            explanation = "Canada borders three oceans: the Atlantic to the east, the Pacific to the west, and the Arctic to the north.",
            topicGroupId = "orig_dup_group_24"
        ),
        Question(
            id = 301,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "When must a federal election be held according to legislation passed by Parliament?",
            options = listOf("When the King wants to replace the Prime Minister", "Within 4 years of the most recent election", "Within 5 years of the last election", "The Prime Minister can call the election any time at his own will"),
            correctAnswerIndex = 1,
            explanation = "The Canada Elections Act's fixed-date election provision (added 2007) sets a 4-year cycle; note the Constitution (Charter s.4) sets a separate 5-year maximum, but the question specifically asks about \"legislation passed by Parliament,\" which points to the fixed-date law (4 years), not the constitutional maximum. (Discover Canada, page 30.)",
            topicGroupId = "federal_election_fixed_date_law"
        ),
        Question(
            id = 302,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is the federal government responsible for?",
            flashcardText = "What is the federal government responsible for?",
            options = listOf("Highways", "Natural resources", "Education", "Interprovincial Trade and Communications"),
            correctAnswerIndex = 3,
            explanation = "Highways, natural resources, and education are provincial responsibilities; interprovincial trade/communications is federal per Discover Canada's federal/provincial jurisdiction list. (Discover Canada, page 28.)",
            topicGroupId = "q302"
        ),
        Question(
            id = 303,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What was the name of the new country formed at Confederation?",
            options = listOf("Britain", "Canada", "Canadian Confederation", "Dominion of Canada"),
            correctAnswerIndex = 3,
            explanation = "Well-documented (1867). (Discover Canada, page 18.)",
            topicGroupId = "q303"
        ),
        Question(
            id = 304,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Where do more than half of the people in Canada live?",
            options = listOf("Coastal Pacific", "Atlantic Canada", "Prairies", "Central Canada"),
            correctAnswerIndex = 3,
            explanation = "Discover Canada states more than half of Canadians live in Central Canada (Ontario and Quebec). (Discover Canada, page 47.)",
            topicGroupId = "q304"
        ),
        Question(
            id = 305,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who brought Quebec into Confederation?",
            options = listOf("Sir Louis-Hippolyte La Fontaine", "Sir George-Étienne Cartier", "Sir Wilfrid Laurier", "Sir John Alexander Macdonald"),
            correctAnswerIndex = 1,
            explanation = "Well-documented Father of Confederation for Quebec/Canada East. (Discover Canada, page 19.)",
            topicGroupId = "q305"
        ),
        Question(
            id = 306,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In World War II, how did Canada contribute more to the Allied air effort than any other Commonwealth country?",
            options = listOf("Trained 130,000 allied aircrew", "Deployed paratroopers in France", "Provided ammunition", "Sent 130,000 soldiers to take France back from the Germans"),
            correctAnswerIndex = 0,
            explanation = "Refers to the British Commonwealth Air Training Plan, which trained over 130,000 aircrew in Canada. (Discover Canada, page 23.)",
            topicGroupId = "q306"
        ),
        Question(
            id = 307,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How can a party in power be defeated in Parliament?",
            options = listOf("If there is a revolution", "If the King orders the party to resign", "If a majority of the MPs vote against a major government decision", "If a minority of the MPs vote against a major government decision"),
            correctAnswerIndex = 2,
            explanation = "— this describes losing a confidence vote. (Discover Canada, page 31.)",
            topicGroupId = "q307"
        ),
        Question(
            id = 308,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following are the responsibilities of provincial government?",
            flashcardText = "What are the responsibilities of provincial government?",
            options = listOf("Education, health care, natural resources, and policing", "National defence, health care, citizenship, and firefighting", "Education, foreign policy, natural resources, and policing", "National defence, foreign policy, highways, and aboriginal affairs"),
            correctAnswerIndex = 0,
            explanation = "Matches Discover Canada's list of provincial jurisdictions (education, health, natural resources, administration of justice/policing). (Discover Canada, page 33.)",
            topicGroupId = "q308"
        ),
        Question(
            id = 309,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What was the \"Underground Railroad\"?",
            options = listOf("An anti-slavery network that helped thousands of slaves escape the United States and settle in Canada", "A railroad through the Rockies that was mainly through mountain tunnels", "A network fur traders used to transport beaver pelts to the United States", "The first underground subway tunnel in Toronto"),
            correctAnswerIndex = 0,
            explanation = "Well-documented historical fact in Discover Canada. (Discover Canada, page 16.)",
            topicGroupId = "q309"
        ),
        Question(
            id = 310,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the name of the Royal Anthem of Canada?",
            options = listOf("Great Canada", "O Canada", "God Save the Queen (or King)", "Oh Canada"),
            correctAnswerIndex = 2,
            explanation = "\"O Canada\" is the National Anthem; \"God Save the King/Queen\" is the Royal Anthem — explicitly distinguished in Discover Canada. (Discover Canada, page 40.)",
            topicGroupId = "q310"
        ),
        Question(
            id = 311,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the primary role of the police in Canada?",
            options = listOf("To resolve disputes and interpret law", "To keep people safe and to enforce the law", "To provide national security intelligence to the government", "To conduct or support land warfare, peacekeeping, or humanitarian missions"),
            correctAnswerIndex = 1,
            explanation = "The correct answer is: To keep people safe and to enforce the law. (Discover Canada, page 37.)",
            topicGroupId = "q311"
        ),
        Question(
            id = 312,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which province has the most bilingual Canadians?",
            options = listOf("British Columbia", "Quebec", "Ontario", "New Brunswick"),
            correctAnswerIndex = 1,
            explanation = "Quebec has by far the largest absolute number of English-French bilingual residents, even though New Brunswick is Canada's only officially bilingual province.",
            topicGroupId = "q312"
        ),
        Question(
            id = 313,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Where is Canada located?",
            options = listOf("Central America", "Europe", "North America", "South America"),
            correctAnswerIndex = 2,
            explanation = "The correct answer is: North America.",
            topicGroupId = "q313"
        ),
        Question(
            id = 314,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Can you name the five Great Lakes between Canada and the U.S.?",
            options = listOf("Lake Toronto, Lake Michigan, Lake Mexico, Lake Ontario, Lake St. Louis", "Lake Superior, Lake Michigan, Lake Huron, Lake Erie, Lake Ontario", "Lake Michigan, Lake Victoria, Lake Mexico, Lake Ontario, Lake St. Louis", "None of the above"),
            correctAnswerIndex = 1,
            explanation = "The correct answer is: Lake Superior, Lake Michigan, Lake Huron, Lake Erie, Lake Ontario.",
            topicGroupId = "q314"
        ),
        Question(
            id = 315,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How are Members of Parliament chosen?",
            options = listOf("Elected by senators", "Elected by the Prime Minister", "Chosen by the King", "Elected by Canadian citizens"),
            correctAnswerIndex = 3,
            explanation = "The correct answer is: Elected by Canadian citizens. (Discover Canada, page 52.)",
            topicGroupId = "mps_elected_by_the_people"
        ),
        Question(
            id = 316,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In what jobs did the Métis first work with European settlers?",
            options = listOf("Supplies, traders, guides, and interpreters", "Taking care of children", "Building housing", "Fishing"),
            correctAnswerIndex = 0,
            explanation = "Matches Discover Canada's description of early Métis roles (guides, interpreters, suppliers, traders).",
            topicGroupId = "q316"
        ),
        Question(
            id = 317,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does the National Register of Electors contain?",
            options = listOf("Database of landed immigrants", "Database of Canadian citizens at least 18 years of age who are qualified to vote in federal elections and referendums", "Database of all Canadian citizens", "Database of Canadian taxpayers"),
            correctAnswerIndex = 1,
            explanation = "The correct answer is: Database of Canadian citizens at least 18 years of age who are qualified to vote in federal elections and referendums. (Discover Canada, page 30.)",
            topicGroupId = "register_of_electors_contents"
        ),
        Question(
            id = 318,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What was the main advantage of the NAFTA agreement?",
            options = listOf("Free trade among Canada, the USA, and Mexico", "Free trade between Canada and China", "Free trade between Canada and the UK", "Free trade between Canada and Japan"),
            correctAnswerIndex = 0,
            explanation = "The correct answer is: Free trade among Canada, the USA, and Mexico.",
            topicGroupId = "q318"
        ),
        Question(
            id = 319,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which Act granted, for the first time in Canada, legislative assemblies elected by the people?",
            options = listOf("The Constitutional Act of 1982", "The Constitutional Act of 1891", "The Constitutional Act of 1791", "The Constitutional Act of 1972"),
            correctAnswerIndex = 2,
            explanation = "The Constitutional Act, 1791 created Upper and Lower Canada, each with an elected legislative assembly. (Discover Canada, page 16.)",
            topicGroupId = "q319"
        ),
        Question(
            id = 320,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does the Crown mean for Canadians?",
            options = listOf("The Crown is a symbol of government, including Parliament, legislatures, courts, police services, and the armed forces.", "The Crown contains symbols of England, France, Scotland, and Ireland, as well as red maple leaves.", "A national motto, A Mari Usque Ad Mare, which, in Latin, means 'from sea to sea.'", "The Crown reflects the Greco-Roman heritage of Western civilization in which democracy originated."),
            correctAnswerIndex = 0,
            explanation = "Closely matches Discover Canada's description of the Crown's symbolic role. (Option B describes the Royal Arms/Coat of Arms, not \"the Crown\" itself; C and D refer to unrelated symbols/heritage facts.). (Discover Canada, page 38.)",
            topicGroupId = "crown_symbol_of_government"
        ),
        Question(
            id = 321,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What did the Suffrage Movement achieve?",
            options = listOf("Quebec experienced an era of rapid change", "The Suffrage Movement abolished slavery in Canada", "The Suffrage Movement led to the introduction of employment insurance", "Women achieved the right to vote"),
            correctAnswerIndex = 3,
            explanation = "The correct answer is: Women achieved the right to vote. (Discover Canada, page 21.)",
            topicGroupId = "q321"
        ),
        Question(
            id = 322,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "When did settlers from France first establish communities on the St. Lawrence River?",
            options = listOf("Late 1600s", "Early 1700s", "Late 1700s", "Early 1600s"),
            correctAnswerIndex = 3,
            explanation = "Quebec City was founded by Champlain in 1608. (Discover Canada, page 15.)",
            topicGroupId = "q322"
        ),
        Question(
            id = 323,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which region is known as the industrial and manufacturing heartland of Canada?",
            options = listOf("Atlantic provinces", "Prairie Provinces", "Central Canada", "West Coast"),
            correctAnswerIndex = 2,
            explanation = "The correct answer is: Central Canada. (Discover Canada, page 47.)",
            topicGroupId = "q323"
        ),
        Question(
            id = 324,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What types of jobs are provided by service industries?",
            options = listOf("Communications and retail services", "Transportation and education", "Tourism and government", "All answers are correct"),
            correctAnswerIndex = 3,
            explanation = "— all listed sectors are service industries.",
            topicGroupId = "q324"
        ),
        Question(
            id = 325,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "With which words does the Canadian Charter of Rights and Freedoms begin?",
            options = listOf("\"Canadian citizens have rights and responsibilities\"", "\"O Canada! Our home and native land!\"", "\"Canada is a free country and home of the brave\"", "\"Whereas Canada is founded upon principles that recognize the supremacy of God and the rule of law\""),
            correctAnswerIndex = 3,
            explanation = "— this is the verbatim opening of the Charter's preamble. (Discover Canada, page 8.)",
            topicGroupId = "charter_preamble_two_principles"
        ),
        Question(
            id = 326,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Why was the Canadian Pacific Railway built?",
            options = listOf("The railway made it possible for immigrants to settle in Central Canada.", "British Columbia joined Canada in 1871 after Ottawa promised to build a railway to the West Coast.", "To provide a spectacular tourist excursion across precipitous passes and bridges", "So British Columbia could handle the trade of goods worth billions of dollars all around the world."),
            correctAnswerIndex = 1,
            explanation = "Matches Discover Canada's account of the CPR and BC's 1871 entry into Confederation. (Discover Canada, page 20.)",
            topicGroupId = "q326"
        ),
        Question(
            id = 327,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Why is trade with other countries important to Canada?",
            options = listOf("Trade with other countries changed the native way of life forever.", "To increase trade and enjoy one of the world's highest standards of living", "Canada has become a member of the World Trade Organization.", "The French and Aboriginal people collaborated with Canada in the vast fur-trade economy."),
            correctAnswerIndex = 1,
            explanation = "The correct answer is: To increase trade and enjoy one of the world's highest standards of living. (Discover Canada, page 24.)",
            topicGroupId = "q327"
        ),
        Question(
            id = 328,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following best describes the role of the King in Canada?",
            flashcardText = "What is the role of the King in Canada?",
            options = listOf("To make important decisions about how the country is governed", "To peacefully oppose or try to improve government proposals", "To run the federal government departments", "To focus on citizenship and allegiance, be a symbol of Canadian sovereignty, and a guardian of constitutional freedoms"),
            correctAnswerIndex = 3,
            explanation = "(Option B describes the role of the Loyal Opposition, not the Sovereign.). (Discover Canada, page 29.)",
            topicGroupId = "q328"
        ),
        Question(
            id = 329,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does it mean to say Canada is a constitutional monarchy?",
            options = listOf("The Sovereign (Queen or King) approves bills before they become law.", "The Sovereign (Queen or King) represents Canadians in Parliament.", "Canada's Head of State is a hereditary Sovereign (Queen or King) who reigns in accordance with the Constitution.", "The Sovereign (Queen or King) is the lawmaker of Canada."),
            correctAnswerIndex = 2,
            explanation = "The correct answer is: Canada's Head of State is a hereditary Sovereign (Queen or King) who reigns in accordance with the Constitution.. (Discover Canada, page 29.)",
            topicGroupId = "q329"
        ),
        Question(
            id = 330,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Remembrance Day is celebrated on:",
            options = listOf("July 1st", "July 4th", "November 11th", "November 20th"),
            correctAnswerIndex = 2,
            explanation = "The correct answer is: November 11th.",
            topicGroupId = "q330"
        ),
        Question(
            id = 331,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What are the provinces of Central Canada?",
            options = listOf("Ontario and Quebec", "Ontario and Alberta", "Quebec and New Brunswick", "Alberta and Saskatchewan"),
            correctAnswerIndex = 0,
            explanation = "The correct answer is: Ontario and Quebec. (Discover Canada, page 47.)",
            topicGroupId = "q331"
        ),
        Question(
            id = 332,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What do the initials MP stand for in Canadian politics?",
            options = listOf("Member of Parliament", "Minister of Parliament", "Member of the Patriots", "Master of the Province"),
            correctAnswerIndex = 0,
            explanation = "Standard, well-documented term. (Discover Canada, page 30.)",
            topicGroupId = "mp_initials_meaning"
        ),
        Question(
            id = 333,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What should you do if you do not receive a voter information card before an election?",
            options = listOf("Call your local municipality.", "Turn up at your nearest polling station on election day.", "You should assume you were not chosen to vote.", "Contact Elections Canada."),
            correctAnswerIndex = 3,
            explanation = "The correct answer is: Contact Elections Canada.. (Discover Canada, page 32.)",
            topicGroupId = "q333"
        ),
        Question(
            id = 334,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What are the parties that are not in power called?",
            options = listOf("Tea parties", "Opposition parties", "Rival parties", "Opponents parties"),
            correctAnswerIndex = 1,
            explanation = "The correct answer is: Opposition parties. (Discover Canada, page 31.)",
            topicGroupId = "q334"
        ),
        Question(
            id = 335,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is the oldest colony of the British Empire in Canada?",
            flashcardText = "What is the oldest colony of the British Empire in Canada?",
            options = listOf("Quebec", "Ontario", "Alberta", "Newfoundland and Labrador"),
            correctAnswerIndex = 3,
            explanation = "Newfoundland is commonly cited as Britain's oldest colony (English claim by Humphrey Gilbert in 1583). (Discover Canada, page 46.)",
            topicGroupId = "q335"
        ),
        Question(
            id = 336,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who is the King's representative in Canada?",
            options = listOf("The Premier", "The Prime Minister's spouse", "The Governor General of Canada", "The Prime Minister"),
            correctAnswerIndex = 2,
            explanation = "The correct answer is: The Governor General of Canada. (Discover Canada, page 34.)",
            topicGroupId = "q336"
        ),
        Question(
            id = 337,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Why are the Great Lakes important to Canada?",
            options = listOf("They provide water for irrigation.", "They provide fresh water and waterways.", "They provide waterways.", "They are tourist attractions."),
            correctAnswerIndex = 1,
            explanation = "The correct answer is: They provide fresh water and waterways..",
            topicGroupId = "q337"
        ),
        Question(
            id = 338,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which province in Canada is the smallest in land size?",
            options = listOf("British Columbia", "Prince Edward Island", "Alberta", "New Brunswick"),
            correctAnswerIndex = 1,
            explanation = "The correct answer is: Prince Edward Island. (Discover Canada, page 46.)",
            topicGroupId = "q338"
        ),
        Question(
            id = 339,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who was the first person to draw a map of Canada's east coast?",
            options = listOf("Jean Talon", "Georges Cartier", "John Cabot", "Jacques Cartier"),
            correctAnswerIndex = 2,
            explanation = "Cabot explored and charted the Atlantic/Newfoundland coast in 1497, prior to Jacques Cartier's 1534 voyages to the St. Lawrence. \"Georges Cartier\" is not a standard historical figure (likely a distractor conflating Jacques Cartier and George-Etienne Cartier). (Discover Canada, page 14.)",
            topicGroupId = "q339"
        ),
        Question(
            id = 340,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "If you cannot pay for a lawyer, how can you get legal help?",
            options = listOf("Borrow money from the government and pay for the lawyer.", "Go to legal aid services in most communities.", "Apply for financial aid from the government to pay for legal fees.", "Do not go to a court."),
            correctAnswerIndex = 1,
            explanation = "The correct answer is: Go to legal aid services in most communities.. (Discover Canada, page 37.)",
            topicGroupId = "q340"
        ),
        Question(
            id = 341,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is a polling station?",
            options = listOf("Place where you vote", "Campaign offices for candidates", "Place where the number of votes is counted", "Member of Parliament's constituency"),
            correctAnswerIndex = 0,
            explanation = "The correct answer is: Place where you vote.",
            topicGroupId = "polling_station_definition"
        ),
        Question(
            id = 342,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is a Cabinet Minister?",
            options = listOf("Candidate picked by the Prime Minister", "MP picked by the Premier of each province", "MP selected by the Prime Minister to run federal departments", "MP selected by the King to make laws"),
            correctAnswerIndex = 2,
            explanation = "The correct answer is: MP selected by the Prime Minister to run federal departments.",
            topicGroupId = "q342"
        ),
        Question(
            id = 343,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who discovered insulin?",
            options = listOf("Dr. Wilder Penfield", "Matthew Evans and Henry Woodward", "Sir Frederick Banting and Charles Best", "Dr. John A. Hopps"),
            correctAnswerIndex = 2,
            explanation = "Well-documented Canadian achievement (1921-22, University of Toronto). (Discover Canada, page 53.)",
            topicGroupId = "q343"
        ),
        Question(
            id = 344,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is a major river in Quebec?",
            options = listOf("Hudson's Bay", "Niagara", "Fraser River", "St. Lawrence River"),
            correctAnswerIndex = 3,
            explanation = "The correct answer is: St. Lawrence River. (Discover Canada, page 47.)",
            topicGroupId = "q344"
        ),
        Question(
            id = 345,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who circled the globe in a wheelchair to raise funds for spinal cord research?",
            options = listOf("Reginald Fessenden", "Rick Hansen", "Terry Fox", "Gerhard Herzberg"),
            correctAnswerIndex = 1,
            explanation = "Rick Hansen's \"Man in Motion World Tour\" (1985-87). Terry Fox is a common distractor but ran on foot/prosthetic for one leg (Marathon of Hope), not a global wheelchair circuit. (Discover Canada, page 26.)",
            topicGroupId = "q345"
        ),
        Question(
            id = 346,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who is the father of Manitoba and defender of Métis rights?",
            options = listOf("Louis Riel", "Sir Louis-Hippolyte La Fontaine", "Sir John Alexander Macdonald", "Sir William Riel"),
            correctAnswerIndex = 0,
            explanation = "The correct answer is: Louis Riel. (Discover Canada, page 19.)",
            topicGroupId = "q346"
        ),
        Question(
            id = 347,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Why is the North sometimes called the 'Land of the Midnight Sun'?",
            options = listOf("It is closer to the Sun.", "The Northern lights appear at midnight.", "It is night most of the time.", "Summer daylight can last up to 24 hours."),
            correctAnswerIndex = 3,
            explanation = "The correct answer is: Summer daylight can last up to 24 hours.. (Discover Canada, page 50.)",
            topicGroupId = "q347"
        ),
        Question(
            id = 348,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "One-third of all Canadians live in:",
            options = listOf("Saskatchewan", "Quebec", "Alberta", "Ontario"),
            correctAnswerIndex = 3,
            explanation = "Discover Canada notes more than one-third of Canadians live in Ontario.",
            topicGroupId = "ontario_population_third_of_canada"
        ),
        Question(
            id = 349,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who can ask you about whom you voted for?",
            options = listOf("No one", "Any other Canadian", "Your local MP", "The Prime Minister"),
            correctAnswerIndex = 0,
            explanation = "Canada uses a secret ballot.",
            topicGroupId = "secret_ballot_no_disclosure"
        ),
        Question(
            id = 350,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "When did the United Empire Loyalists come to Canada?",
            options = listOf("Late 1600s", "Early 1600s", "Early 1700s", "Late 1700s"),
            correctAnswerIndex = 3,
            explanation = "Loyalists arrived mainly 1783-1785, following the American Revolution.",
            topicGroupId = "q350"
        ),
        Question(
            id = 351,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What happens when the federal government loses a confidence vote?",
            options = listOf("An election is called.", "The official opposition party takes power.", "The Prime Minister loses his job.", "The Prime Minister is no longer the leader of his party."),
            correctAnswerIndex = 0,
            explanation = "Discover Canada states that after losing a confidence vote the government typically resigns or asks the Governor General to dissolve Parliament and call an election; \"an election is called\" is the standard test answer among these options, though resignation is technically the immediate constitutional step. (Discover Canada, page 18.)",
            topicGroupId = "confidence_vote_consequence_group"
        ),
        Question(
            id = 352,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who has the right to apply for a Canadian passport?",
            options = listOf("British citizens", "Canadian citizens", "Any immigrant who has stayed a minimum of 3 years in Canada", "Wealthy citizens"),
            correctAnswerIndex = 1,
            explanation = "The correct answer is: Canadian citizens.",
            topicGroupId = "q352"
        ),
        Question(
            id = 353,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who were the Group of Seven in modern Canada?",
            options = listOf("A group of politicians", "A group of Canadian companies", "A group of cowboys who defended Canada", "A group of Canadian landscape painters in the 1920s"),
            correctAnswerIndex = 3,
            explanation = "The correct answer is: A group of Canadian landscape painters in the 1920s. (Discover Canada, page 25.)",
            topicGroupId = "q353"
        ),
        Question(
            id = 354,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What level of government passes \"by-laws\"?",
            options = listOf("Provincial", "Municipal or local government", "Federal", "Senators"),
            correctAnswerIndex = 1,
            explanation = "The correct answer is: Municipal or local government.",
            topicGroupId = "q354"
        ),
        Question(
            id = 355,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In which type of industry did most early European settlers work?",
            options = listOf("Fur trading", "Oil", "Gold mining", "Hunting"),
            correctAnswerIndex = 0,
            explanation = "The correct answer is: Fur trading.",
            topicGroupId = "q355"
        ),
        Question(
            id = 356,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How is a Cabinet Minister chosen?",
            options = listOf("By the Prime Minister", "By the King", "By the voters", "By the senators"),
            correctAnswerIndex = 0,
            explanation = "Consistent with Q61.",
            topicGroupId = "q356"
        ),
        Question(
            id = 357,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does M.N.A. stand for?",
            options = listOf("Member of the National Aid", "Member of the National Association", "Member of the National Airline", "Member of the National Assembly"),
            correctAnswerIndex = 3,
            explanation = "Term used for members of Quebec's provincial legislature. (Discover Canada, page 33.)",
            topicGroupId = "q357"
        ),
        Question(
            id = 358,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "The amended Constitution of Canada in 1982 was proclaimed by:",
            options = listOf("The Senate", "Queen Elizabeth II", "The people of Canada", "The Prime Minister"),
            correctAnswerIndex = 1,
            explanation = "Queen Elizabeth II signed the Proclamation of the Constitution Act in Ottawa on April 17, 1982. (Discover Canada, page 8.)",
            topicGroupId = "q358"
        ),
        Question(
            id = 359,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is Canada's system of government called?",
            options = listOf("Dictatorship", "Parliamentary government", "Military Rule", "Communism"),
            correctAnswerIndex = 1,
            explanation = "The correct answer is: Parliamentary government. (Discover Canada, page 28.)",
            topicGroupId = "q359"
        ),
        Question(
            id = 360,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "From where did the first European settlers in Canada come?",
            options = listOf("Germany", "England", "France", "Iceland"),
            correctAnswerIndex = 2,
            explanation = "The French established the first permanent European settlements in Canada (Port Royal 1605, Quebec 1608). Norse Vikings visited earlier (~1000 AD) but did not leave a lasting settlement, so they are not the \"settlers\" referenced here.",
            topicGroupId = "q360"
        ),
        Question(
            id = 361,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How can you contact your Member of Parliament?",
            options = listOf("By using social media sites online", "By writing a letter to the House of Commons", "By booking an appointment over the phone", "By waiting outside of the Parliament building"),
            correctAnswerIndex = 1,
            explanation = "Moderately confident. Discover Canada notes MPs can be reached free of charge by mail addressed to them at the House of Commons, Ottawa - this is the classic guide fact behind this question, though the question could arguably be read more loosely.",
            topicGroupId = "q361"
        ),
        Question(
            id = 362,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who was Sir George-Étienne Cartier?",
            options = listOf("A railway lawyer and Montrealer", "The first French-Canadian Prime Minister", "The first head of a responsible government", "Canada's first Prime Minister"),
            correctAnswerIndex = 0,
            explanation = "Cartier was a Father of Confederation described in Discover Canada as a railway lawyer and Montrealer, and joint premier with John A. Macdonald. He was never PM (that was Macdonald, option D); the first French-Canadian PM was Wilfrid Laurier (option B), not Cartier. (Discover Canada, page 19.)",
            topicGroupId = "q362"
        ),
        Question(
            id = 363,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who is the greatest Canadian hockey player?",
            options = listOf("Wayne Gretzky", "Mark Tewksbury", "Donovan Bailey", "Terry Fox"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada refers to Wayne Gretzky, \"The Great One,\" as one of the greatest hockey players ever. The other names are an Olympic swimmer, an Olympic sprinter, and a distance runner/cancer research icon, not hockey players. (Discover Canada, page 26.)",
            topicGroupId = "q363"
        ),
        Question(
            id = 364,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following answers is NOT true about the relationship between Canada and the USA?",
            flashcardText = "Which statement about the relationship between Canada and the USA is NOT true?",
            options = listOf("Canada and the USA share the longest undefended international border.", "Canada and the USA are the largest trading partners in the world.", "Canada exports very few goods to the USA.", "The relationship between Canada and the USA is the closest and the most extensive in the world."),
            correctAnswerIndex = 2,
            explanation = "The guide states the opposite - the vast majority of Canada's exports go to the USA, and describes the border, trade relationship, and closeness as stated in A, B, and D.",
            topicGroupId = "q364"
        ),
        Question(
            id = 365,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the significance of hockey?",
            options = listOf("It is the national summer sport.", "It is the most popular spectator sport in Canada and also its national winter sport.", "Canada won a gold medal in 2008's Olympics in this event.", "None of these"),
            correctAnswerIndex = 1,
            explanation = "Hockey is Canada's official national winter sport and most popular spectator sport per Discover Canada. Option C is also factually wrong (Canada's men's hockey gold was 2002 and 2010, not 2008 - no Winter Olympics in 2008). (Discover Canada, page 39.)",
            topicGroupId = "hockey_most_popular_spectator_sport"
        ),
        Question(
            id = 366,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What are the two principles upon which Canada is founded?",
            options = listOf("The supremacy of God and the rule of law", "The supremacy of God and freedom of speech", "The supremacy of law and the rule of God", "Mobility right and the rule of law"),
            correctAnswerIndex = 0,
            explanation = "This is directly from the preamble of the Canadian Charter of Rights and Freedoms, quoted in Discover Canada: \"Canada is founded upon principles that recognize the supremacy of God and the rule of law.\". (Discover Canada, page 8.)",
            topicGroupId = "charter_preamble_two_principles"
        ),
        Question(
            id = 367,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Since when has the protocol for the amendment of the Canadian constitution existed?",
            options = listOf("1962", "1982", "1885", "1972"),
            correctAnswerIndex = 1,
            explanation = "The Constitution Act, 1982 (which included the Charter of Rights and Freedoms and patriated the constitution) established the formal amending formula.",
            topicGroupId = "q367"
        ),
        Question(
            id = 368,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What are the main functions of the Cabinet?",
            options = listOf("Natural resources", "Navigation", "To prepare the budget and propose new laws to be implemented", "Defence"),
            correctAnswerIndex = 2,
            explanation = "Cabinet's core role per Discover Canada is preparing the budget and proposing most legislation for Parliament. Options A, B, D are federal jurisdiction topics, not Cabinet \"functions.\"",
            topicGroupId = "q368"
        ),
        Question(
            id = 369,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "If the government loses a confidence vote in the assembly, it must ____.",
            options = listOf("call for by-elections", "continue governing", "do nothing", "resign"),
            correctAnswerIndex = 3,
            explanation = "Loss of a confidence vote requires the government to resign (potentially triggering a general election), a standard parliamentary/responsible government principle covered in Discover Canada. (Discover Canada, page 18.)",
            topicGroupId = "confidence_vote_consequence_group"
        ),
        Question(
            id = 370,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which province was the first to grant voting rights to women?",
            options = listOf("Québec", "Ontario", "Nova Scotia", "Manitoba"),
            correctAnswerIndex = 3,
            explanation = "Manitoba became the first province to grant women the right to vote provincially in January 1916. (Discover Canada, page 21.)",
            topicGroupId = "q370"
        ),
        Question(
            id = 371,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the Great Charter of Freedom also known as?",
            options = listOf("Habeas Corpus", "Dominion of Canada", "Canadian Constitution", "Magna Carta"),
            correctAnswerIndex = 3,
            explanation = "The Magna Carta (1215) is referred to as the \"Great Charter\" and is cited in Discover Canada as part of the British heritage of Canadian rights and law. (Discover Canada, page 8.)",
            topicGroupId = "q371"
        ),
        Question(
            id = 372,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "A member of Parliament from Montreal announces that she will spend her weekend in her electoral district. This means she would be ____.",
            options = listOf("in her office on Parliament Hill", "in some part of Montreal where she was elected", "visiting the province of Québec", "going on a vacation"),
            correctAnswerIndex = 1,
            explanation = "An MP's electoral district (riding) is the specific local area they represent, so \"spending the weekend in her electoral district\" means being in the part of Montreal she was elected to represent - straightforward reasoning from the concept of ridings covered in Discover Canada.",
            topicGroupId = "q372"
        ),
        Question(
            id = 373,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How is the government formed after a federal election?",
            options = listOf("Each province elects one representative to form the government. The King then chooses the Prime Minister.", "The Governor General picks a party and a Prime Minister to run the government.", "The party with the most elected representatives becomes the party in power. The leader of this party becomes the Prime Minister.", "The party with the most elected representatives becomes the party in power. The King chooses the Prime Minister from this party."),
            correctAnswerIndex = 2,
            explanation = "This is standard responsible-government/Westminster process described in Discover Canada: the party winning the most seats forms government and its leader becomes PM. (Discover Canada, page 31.)",
            topicGroupId = "q373"
        ),
        Question(
            id = 374,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who chose Ottawa as the capital of Canada?",
            options = listOf("Queen Elizabeth I", "Queen Elizabeth II", "Queen Victoria", "Queen Anne"),
            correctAnswerIndex = 2,
            explanation = "Queen Victoria chose Ottawa (then Bytown) as the capital of the Province of Canada in 1857, a well-documented fact repeated in Discover Canada.",
            topicGroupId = "q374"
        ),
        Question(
            id = 375,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In which province are more than half of Canada's aeronautics and space industry located?",
            options = listOf("Saskatchewan", "Ontario", "Québec", "Manitoba"),
            correctAnswerIndex = 2,
            explanation = "Discover Canada's Quebec regional profile notes Quebec, especially Montreal, is home to more than half of Canada's aerospace industry.",
            topicGroupId = "q375"
        ),
        Question(
            id = 376,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who is General Sir Arthur Currie?",
            options = listOf("Canada's greatest soldier in the First World War", "A great frontier hero", "An explorer of western Canada", "A military leader of the Métis in the 19th century"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada's military history section describes General Sir Arthur Currie as Canada's greatest soldier of the First World War, commander of the Canadian Corps. (Discover Canada, page 21.)",
            topicGroupId = "currie_greatest_soldier_ww1"
        ),
        Question(
            id = 377,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which province was split into two at Confederation?",
            options = listOf("Lower Canada", "Newfoundland", "Upper Canada", "The Province of Canada"),
            correctAnswerIndex = 3,
            explanation = "The united Province of Canada (which had combined Upper and Lower Canada) was split back into Ontario and Quebec at Confederation in 1867. (Discover Canada, page 18.)",
            topicGroupId = "q377"
        ),
        Question(
            id = 378,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does the word \"Inuit\" mean?",
            options = listOf("\"Eskimo\" in the Inuktitut language", "\"Home\" in English", "\"The people\" in the Inuktitut language", "\"The Arctic Land\" in the Inuktitut language"),
            correctAnswerIndex = 2,
            explanation = "Discover Canada states \"Inuit\" means \"the people\" in Inuktitut, and notes \"Eskimo\" is an outdated/inappropriate term no longer used. (Discover Canada, page 11.)",
            topicGroupId = "q378"
        ),
        Question(
            id = 379,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which provinces are connected to Ontario by land?",
            options = listOf("New Brunswick and Québec", "Alberta and Québec", "Manitoba and Québec", "Manitoba and Alberta"),
            correctAnswerIndex = 2,
            explanation = "Based on standard Canadian geography - Ontario shares land borders with Manitoba to the west and Québec to the east.",
            topicGroupId = "q379"
        ),
        Question(
            id = 380,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who do Members of Parliament represent?",
            options = listOf("Everyone who lives in his or her electoral district", "Everyone who lives in his or her neighborhood", "Everyone who lives in his or her province", "Everyone in Northern Canada"),
            correctAnswerIndex = 0,
            explanation = "Standard definition of an MP's constituency role in Discover Canada.",
            topicGroupId = "q380"
        ),
        Question(
            id = 381,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the role of the Opposition Parties?",
            options = listOf("To ensure reports about the current government are sent to the King", "To supervise the government", "To oppose or try to improve government proposals", "To regulate government proposals"),
            correctAnswerIndex = 2,
            explanation = "Discover Canada describes the opposition's role as questioning and trying to improve or oppose government bills and actions. (Discover Canada, page 31.)",
            topicGroupId = "q381"
        ),
        Question(
            id = 382,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Why is the British North America Act important in Canadian history?",
            options = listOf("It was agreed to by the First Nations and Inuit.", "It was written by the British government.", "The Aboriginal people signed the act.", "It made Confederation legal."),
            correctAnswerIndex = 3,
            explanation = "The British North America Act (1867), passed by the British Parliament, is the legal act that created the Dominion of Canada (Confederation). It was not an agreement signed by First Nations/Aboriginal peoples. (Discover Canada, page 46.)",
            topicGroupId = "q382"
        ),
        Question(
            id = 383,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who built the French Empire in North America?",
            options = listOf("King Charles II", "Jean Talon, Bishop Laval, and Count Frontenac", "Pierre Le Moyne, Sieur d'Iberville", "Great Britain"),
            correctAnswerIndex = 1,
            explanation = "Discover Canada credits these three leaders: 'Outstanding leaders like Jean Talon, Bishop Laval, and Count Frontenac built a French Empire in North America that reached from Hudson Bay to the Gulf of Mexico.' (Discover Canada, page 15.)",
            topicGroupId = "q383"
        ),
        Question(
            id = 384,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which province is on the Pacific coast of Canada?",
            options = listOf("Nova Scotia", "Alberta", "New Brunswick", "British Columbia"),
            correctAnswerIndex = 3,
            explanation = "Basic Canadian geography confirmed in Discover Canada's regional descriptions. (Discover Canada, page 49.)",
            topicGroupId = "q384"
        ),
        Question(
            id = 385,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How many Great Lakes are located between Ontario and the United States?",
            options = listOf("Four", "Five", "Six", "Seven"),
            correctAnswerIndex = 1,
            explanation = "Discover Canada counts all five as a regional group, even though Lake Michigan lies entirely within the US: 'There are five Great Lakes located between Ontario and the United States: Lake Ontario, Lake Erie, Lake Huron, Lake Michigan (in the U.S.A.) and Lake Superior, the largest freshwater lake in the world.' (Discover Canada, page 48.)",
            topicGroupId = "q385"
        ),
        Question(
            id = 386,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which group of Aboriginal peoples has the largest population in the Northern Territories and Nunavut?",
            options = listOf("Acadians", "Métis", "First Nations", "Inuit"),
            correctAnswerIndex = 3,
            explanation = "Inuit form the majority population in Nunavut and a large share in the territories generally (Acadians are not an Aboriginal group at all).",
            topicGroupId = "q386"
        ),
        Question(
            id = 387,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the significance of the Québec Act of 1774?",
            options = listOf("It allowed Québec to gain independence.", "It allowed the French to move into Québec.", "Canada's tolerance of religious tradition under the law", "It gave the French more power."),
            correctAnswerIndex = 2,
            explanation = "The Quebec Act of 1774 recognized the French language, Catholic religion, and French civil law in Quebec, and is cited in Discover Canada as an early foundation of Canada's tradition of accommodation and tolerance - it did not grant independence.",
            topicGroupId = "q387"
        ),
        Question(
            id = 388,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who has the right to be considered first for a job in the Federal government?",
            options = listOf("Canadian citizens", "Anyone with the relevant experience", "Anyone with the necessary qualifications", "Foreigners"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada states citizens have the right to be considered first for federal jobs.",
            topicGroupId = "q388"
        ),
        Question(
            id = 389,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who are Anglophones?",
            options = listOf("People who were taught English at school", "People who understand but do not speak English", "People who do not speak English as a first language", "People who speak English as a first language"),
            correctAnswerIndex = 3,
            explanation = "Standard definition given in the guide. (Discover Canada, page 11.)",
            topicGroupId = "q389"
        ),
        Question(
            id = 390,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who were the Voyageurs?",
            options = listOf("Montreal-based traders who travelled by canoe", "Immigrants to Canada in the 18th century", "Explorers searching for the Northwest Passage", "Geographers who first charted the coastline of British Columbia"),
            correctAnswerIndex = 0,
            explanation = "Voyageurs were Montreal-based fur traders who travelled the interior by canoe. (Discover Canada, page 15.)",
            topicGroupId = "q390"
        ),
        Question(
            id = 391,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which countries fought in the Battle of the Plains of Abraham?",
            options = listOf("British and German", "British and French", "France and China", "America and British"),
            correctAnswerIndex = 1,
            explanation = "Well-documented 1759 battle between British and French forces. (Discover Canada, page 15.)",
            topicGroupId = "q391"
        ),
        Question(
            id = 392,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which group of Aboriginal peoples has the largest population in Canada?",
            options = listOf("Acadians", "First Nations", "Indigenous peoples", "Métis"),
            correctAnswerIndex = 1,
            explanation = "First Nations are the largest of the three Aboriginal groups (First Nations, Métis, Inuit) per the guide/census data it cites.",
            topicGroupId = "q392"
        ),
        Question(
            id = 393,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What do you call a law before it is passed?",
            options = listOf("A proposed law", "A bill", "A new law", "A proposal of a law"),
            correctAnswerIndex = 1,
            explanation = "Standard civics terminology used in the guide.",
            topicGroupId = "q393"
        ),
        Question(
            id = 394,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who, among these, is a Nobel Prize-winning scientist?",
            options = listOf("Gerhard Herzberg", "Marshall McLuhan", "Alexander Graham Bell", "Harold Innis"),
            correctAnswerIndex = 0,
            explanation = "Herzberg won the Nobel Prize in Chemistry (1971); McLuhan, Bell, and Innis did not win Nobel Prizes. (Discover Canada, page 26.)",
            topicGroupId = "q394"
        ),
        Question(
            id = 395,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Canadians have rights and fundamental freedoms, such as:",
            options = listOf("Thought and belief", "Opinion and expression", "Freedom of religion", "All of the above"),
            correctAnswerIndex = 3,
            explanation = "All listed are fundamental freedoms under the Canadian Charter of Rights and Freedoms as described in the guide.",
            topicGroupId = "q395"
        ),
        Question(
            id = 396,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "When did the British North America Act come into effect?",
            options = listOf("1867", "1881", "1901", "1876"),
            correctAnswerIndex = 0,
            explanation = "The BNA Act took effect July 1, 1867, creating Confederation. (Discover Canada, page 18.)",
            topicGroupId = "confederation_1867"
        ),
        Question(
            id = 397,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the highest honour available to Canadians?",
            options = listOf("The Queen's Medal", "Elizabeth Cross", "Victoria Medal", "Victoria Cross"),
            correctAnswerIndex = 3,
            explanation = "The guide states the Victoria Cross is the highest honour available to Canadians, awarded for extreme bravery. (Discover Canada, page 41.)",
            topicGroupId = "q397"
        ),
        Question(
            id = 398,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which city provides important shipping and air links across the Pacific Ocean?",
            options = listOf("Victoria", "Calgary", "Edmonton", "Vancouver"),
            correctAnswerIndex = 3,
            explanation = "Vancouver is described as Canada's key Pacific gateway for shipping and air links.",
            topicGroupId = "q398"
        ),
        Question(
            id = 399,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is Terry Fox's contribution?",
            options = listOf("He inspired people to contribute money for cancer research.", "He was the greatest hockey player in Canada.", "His discovery of insulin saves millions of people's lives.", "He was a brilliant soldier."),
            correctAnswerIndex = 0,
            explanation = "Terry Fox's Marathon of Hope inspired ongoing cancer research fundraising. (Discover Canada, page 26.)",
            topicGroupId = "q399"
        ),
        Question(
            id = 400,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the head of the city called?",
            options = listOf("Mayor", "Councillor", "Alderman", "Premier"),
            correctAnswerIndex = 0,
            explanation = "Standard civics fact.",
            topicGroupId = "q400"
        ),
        Question(
            id = 401,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In what sorts of jobs do most Canadians work?",
            options = listOf("Service", "Lumbering", "Farming", "Natural resources"),
            correctAnswerIndex = 0,
            explanation = "The guide states most Canadians are employed in service industries.",
            topicGroupId = "q401"
        ),
        Question(
            id = 402,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is written on an election ballot?",
            options = listOf("The names of the candidates in your election district", "Who you should vote for", "The date and time you are allowed to vote", "Where you should vote"),
            correctAnswerIndex = 0,
            explanation = "Standard description of a federal election ballot.",
            topicGroupId = "q402"
        ),
        Question(
            id = 403,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Where do most French-speaking Canadians live?",
            options = listOf("Nova Scotia", "Québec", "Ontario", "New Brunswick"),
            correctAnswerIndex = 1,
            explanation = "The large majority of Francophone Canadians live in Québec.",
            topicGroupId = "q403"
        ),
        Question(
            id = 404,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who started the women's suffrage movement in Canada?",
            options = listOf("Agnes Macphail", "Laura Secord", "Dr. Emily Stowe", "Madeleine Parent"),
            correctAnswerIndex = 2,
            explanation = "Discover Canada credits Dr. Emily Stowe as founder of the women's suffrage movement in Canada; Agnes Macphail was the first woman elected to Parliament, a distinct fact. (Discover Canada, page 21.)",
            topicGroupId = "q404"
        ),
        Question(
            id = 405,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What region is called the 'Land of the Midnight Sun'?",
            options = listOf("Central Canada", "The Northern Territories", "The Prairies", "The Maritimes"),
            correctAnswerIndex = 1,
            explanation = "The North's summer near-continuous daylight gives it this name. (Discover Canada, page 50.)",
            topicGroupId = "q405"
        ),
        Question(
            id = 406,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does it mean for a political party to \"be in power\"?",
            options = listOf("To gain the approval of the Queen or King", "To have the most elected representatives", "To generate electricity", "To hold the nuclear button"),
            correctAnswerIndex = 1,
            explanation = "Standard civics definition of forming government.",
            topicGroupId = "q406"
        ),
        Question(
            id = 407,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "To which of the following communities do the majority of Canadians belong?",
            options = listOf("Christian", "Jewish", "Muslim", "Hindu"),
            correctAnswerIndex = 0,
            explanation = "The guide notes the majority of Canadians identify as Christian. (Discover Canada, page 11.)",
            topicGroupId = "q407"
        ),
        Question(
            id = 408,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which is the northeastern province in Canada that has its own time zone?",
            options = listOf("Alberta", "Newfoundland & Labrador", "Nova Scotia", "Prince Edward Island"),
            correctAnswerIndex = 1,
            explanation = "Newfoundland and Labrador uses the distinct Newfoundland Time Zone (UTC-3:30). (Discover Canada, page 46.)",
            topicGroupId = "q408"
        ),
        Question(
            id = 409,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the voting procedure in Canada?",
            options = listOf("Whichever way you like", "Online", "Secret Ballot", "Open Ballot"),
            correctAnswerIndex = 2,
            explanation = "Canadian federal elections use a secret ballot.",
            topicGroupId = "q409"
        ),
        Question(
            id = 410,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which is the Canadian province with the largest population?",
            options = listOf("Ontario", "Québec", "Nova Scotia", "Alberta"),
            correctAnswerIndex = 0,
            explanation = "Ontario is Canada's most populous province.",
            topicGroupId = "ontario_most_populous_province"
        ),
        Question(
            id = 411,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does C.P.R. stand for?",
            options = listOf("Canadian Pacific Railway", "Canadian People Railway", "Canadian Public Road", "Canadian People Resource"),
            correctAnswerIndex = 0,
            explanation = "The CPR was central to Confederation-era nation-building. (Discover Canada, page 20.)",
            topicGroupId = "q411"
        ),
        Question(
            id = 412,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the fundamental characteristic of Canadian heritage and identity?",
            options = listOf("Multiculturalism", "French Culture", "Canadian festivals", "English culture"),
            correctAnswerIndex = 0,
            explanation = "The guide explicitly identifies multiculturalism as a fundamental characteristic of Canadian heritage and identity. (Discover Canada, page 8.)",
            topicGroupId = "q412"
        ),
        Question(
            id = 413,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the symbol of the Canadian government?",
            options = listOf("The Parliament", "The Crown", "The National Flag", "The Snowbirds"),
            correctAnswerIndex = 1,
            explanation = "The guide identifies the Crown as the symbol of the Canadian government/state authority.",
            topicGroupId = "crown_symbol_of_government"
        ),
        Question(
            id = 414,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who invented the worldwide system of standard time zones?",
            options = listOf("Joseph-Armand Bombardier", "Reginald Fessenden", "Sir Sandford Fleming", "Alexander Graham Bell"),
            correctAnswerIndex = 2,
            explanation = "Sir Sandford Fleming is credited with proposing worldwide standard time zones, a frequently cited fact in the guide. (Discover Canada, page 27.)",
            topicGroupId = "q414"
        ),
        Question(
            id = 415,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "The ancestors of the Aboriginals are believed to have migrated from which of the following continents?",
            options = listOf("Asia", "America", "Europe", "Australia"),
            correctAnswerIndex = 0,
            explanation = "The guide states Aboriginal ancestors are believed to have migrated from Asia (via a land bridge) thousands of years ago. (Discover Canada, page 10.)",
            topicGroupId = "q415"
        ),
        Question(
            id = 416,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "The Municipal Government is responsible for which of the following?",
            options = listOf("Natural resources", "Currency", "Garbage removal", "Highways"),
            correctAnswerIndex = 2,
            explanation = "Garbage/waste collection is a classic municipal responsibility; natural resources and highways are provincial, currency is federal. (Discover Canada, page 33.)",
            topicGroupId = "q416"
        ),
        Question(
            id = 417,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who is the head of the government in Canada?",
            options = listOf("The Sovereign", "The Prime Minister", "The Premier", "The Commissioner"),
            correctAnswerIndex = 1,
            explanation = "The Sovereign/Governor General is head of state; the Prime Minister is head of government. (Discover Canada, page 29.)",
            topicGroupId = "pm_head_of_government"
        ),
        Question(
            id = 418,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What do we need to bring with us for voting?",
            options = listOf("Voter information card, voter's identity, and address proof", "Credit card", "Ballot Paper", "None of these"),
            correctAnswerIndex = 0,
            explanation = "Matches Elections Canada ID requirements referenced in citizenship study material. (Discover Canada, page 32.)",
            topicGroupId = "q418"
        ),
        Question(
            id = 419,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Where have most immigrants come from since the 1970s?",
            options = listOf("Asia", "England", "France", "USA"),
            correctAnswerIndex = 0,
            explanation = "This is stated near-verbatim in Discover Canada.",
            topicGroupId = "q419"
        ),
        Question(
            id = 420,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "For what product did the first companies formed in Canada compete?",
            options = listOf("Timber trade", "Gold trade", "Fur trade", "Fish trade"),
            correctAnswerIndex = 2,
            explanation = "The fur trade drove Canada's earliest commercial companies.",
            topicGroupId = "q420"
        ),
        Question(
            id = 421,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How many Canadians were killed in World War I, from 1914-1918?",
            options = listOf("60,000", "170,000", "200,000", "70,000"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada states: 'In total 60,000 Canadians were killed and 170,000 wounded.' (Discover Canada, page 21.)",
            topicGroupId = "q421"
        ),
        Question(
            id = 422,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How many Canadians have been awarded the Victoria Cross?",
            options = listOf("96", "500", "2", "1222"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada states: 'The V.C. has been awarded to 96 Canadians since 1854.' (Discover Canada, page 41.)",
            topicGroupId = "q422"
        ),
        Question(
            id = 423,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which country was liberated by the Canadian Army in 1944-1945?",
            options = listOf("Germany", "Austria", "The Netherlands", "Japan"),
            correctAnswerIndex = 2,
            explanation = "The liberation of the Netherlands by Canadian forces is a core, well-documented Discover Canada fact. (Discover Canada, page 23.)",
            topicGroupId = "q423"
        ),
        Question(
            id = 424,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who is known as \"the greatest living Canadian\"?",
            options = listOf("Dr. Wilder Penfield", "Terry Fox", "Sir John Alexander Macdonald", "Sir Sandford Fleming"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada names Dr. Wilder Penfield, the renowned McGill neurosurgeon, as \"the greatest living Canadian.\" (Discover Canada, page 27.)",
            topicGroupId = "q424"
        ),
        Question(
            id = 425,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which province has the largest population of Aboriginals?",
            options = listOf("Manitoba", "Ontario", "Nova Scotia", "Alberta"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada states: 'Manitoba is also an important centre of Ukrainian culture, with 14% reporting Ukrainian origins, and the largest Aboriginal population of any province, at over 15%.' (Discover Canada, page 48.)",
            topicGroupId = "q425"
        ),
        Question(
            id = 426,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How large is Canada?",
            options = listOf("About 8 million sq. kilometers", "About 10 million sq. kilometers", "About 11 million sq. kilometers", "About 9 million sq. kilometers"),
            correctAnswerIndex = 1,
            explanation = "Canada's area (~9.98 million km²) is standardly rounded to \"almost 10 million square kilometres\" in Discover Canada.",
            topicGroupId = "q426"
        ),
        Question(
            id = 427,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Where is the most important harbor in Eastern Canada located?",
            options = listOf("Vancouver", "Yellowknife", "Halifax", "Québec"),
            correctAnswerIndex = 2,
            explanation = "Halifax's large natural ice-free harbour is well documented.",
            topicGroupId = "q427"
        ),
        Question(
            id = 428,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who is considered Canada's greatest soldier?",
            options = listOf("General Sir Arthur Currie", "Phil Edwards", "Sir John Alexander Macdonald", "Rick Hansen"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada explicitly describes Currie as considered by many to be Canada's greatest soldier. (Discover Canada, page 21.)",
            topicGroupId = "currie_greatest_soldier_ww1"
        ),
        Question(
            id = 429,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In which Act are the responsibilities of the federal and provincial government defined?",
            options = listOf("The Federal Act", "The Government Act", "The Responsibilities Act", "The Constitution Act"),
            correctAnswerIndex = 3,
            explanation = "The Constitution Act, 1867 divides federal and provincial powers. (Discover Canada, page 28.)",
            topicGroupId = "q429"
        ),
        Question(
            id = 430,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which courts are for civil cases involving small sums of money?",
            options = listOf("The Federal Court", "The Small Claims Courts", "A trial court", "A provincial court"),
            correctAnswerIndex = 1,
            explanation = "Small Claims Court is specifically for minor civil disputes/small sums. (Discover Canada, page 37.)",
            topicGroupId = "q430"
        ),
        Question(
            id = 431,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is a Voter Information Card?",
            options = listOf("A list tells you who the candidates are in your electoral district", "A letter that lets you know the voting schedule", "A form that tells you where and when to vote", "A card to let you register for voting"),
            correctAnswerIndex = 2,
            explanation = "Matches Elections Canada's description of the Voter Information Card. (Discover Canada, page 32.)",
            topicGroupId = "voter_information_card_definition"
        ),
        Question(
            id = 432,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the \"head tax\"?",
            options = listOf("Race-based entry fee charged for Chinese entering Canada", "Fee charged for anyone entering Canada after 1900", "A tax imposed on beer beginning in 1867", "Fee charged for moving westward in the early 1900s"),
            correctAnswerIndex = 0,
            explanation = "The Chinese head tax is a well-documented historical fact covered in Discover Canada. (Discover Canada, page 20.)",
            topicGroupId = "q432"
        ),
        Question(
            id = 433,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following are the responsibilities of the federal government?",
            flashcardText = "What are the responsibilities of the federal government?",
            options = listOf("National defence, foreign policy, international trade, and Aboriginal affairs", "National defence, health care, international trade, and aboriginal affairs", "Highways, policing, international trade, and criminal justice", "Education, foreign policy, recycling programs, and aboriginal affairs"),
            correctAnswerIndex = 0,
            explanation = "Health care and education are primarily provincial, highways/policing mainly provincial/municipal, so A is the cleanest correct set. (Discover Canada, page 33.)",
            topicGroupId = "q433"
        ),
        Question(
            id = 434,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does the Register of Electors contain?",
            options = listOf("A list of all Canadian citizens who are qualified to vote in federal elections and referendums", "A list of people who are willing to vote in elections and referendums", "A list of people who voted for the opposition party in the previous election", "A list of people who are not allowed to vote"),
            correctAnswerIndex = 0,
            explanation = "Standard description of the National Register of Electors. (Discover Canada, page 30.)",
            topicGroupId = "register_of_electors_contents"
        ),
        Question(
            id = 435,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In the Canadian justice system, what are the roles of the courts and the police?",
            options = listOf("The courts make laws and the police enforce them.", "The courts enforce federal laws and the police enforce provincial laws.", "The courts enforce laws, and the police settle disputes.", "The courts settle disputes and the police enforce the laws."),
            correctAnswerIndex = 3,
            explanation = "Matches Discover Canada's description of courts settling disputes and police enforcing/upholding the law. (Discover Canada, page 36.)",
            topicGroupId = "q435"
        ),
        Question(
            id = 436,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the reason behind the Canada and U.S. border?",
            options = listOf("To improve security", "To maintain distance", "Canada wishes to remain independent of the United States.", "To prevent war between the two countries"),
            correctAnswerIndex = 2,
            explanation = "Discover Canada frames the shared border as an expression of Canada's wish to remain independent of the United States. (Discover Canada, page 17.)",
            topicGroupId = "q436"
        ),
        Question(
            id = 437,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the other name for a trial court?",
            options = listOf("The Court of Queen's Bench", "The Federal Court", "The Provincial Court", "The Small Claims Court"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada notes that trial courts are also called Courts of Queen's Bench in several provinces. (Discover Canada, page 37.)",
            topicGroupId = "q437"
        ),
        Question(
            id = 438,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the tenure of the Governor General?",
            options = listOf("4 years", "5 years", "6 years", "7 years"),
            correctAnswerIndex = 1,
            explanation = "The Governor General's tenure is normally about five years, the same term Discover Canada gives for lieutenant governors. (Discover Canada, page 29.)",
            topicGroupId = "q438"
        ),
        Question(
            id = 439,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Postwar, Canada became a more flexible and open society. Which of the following was this based on?",
            options = listOf("Equality of men and women", "Inequality of women", "Inequality of men and women", "Equality of men"),
            correctAnswerIndex = 0,
            explanation = "Straightforward match to Discover Canada's postwar society description. (Discover Canada, page 25.)",
            topicGroupId = "q439"
        ),
        Question(
            id = 440,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which three rights are included in the Canadian Charter of Rights and Freedoms?",
            options = listOf("Freedom of expression rights, property rights and fair trial rights", "Mobility rights, Aboriginal People's rights, and official language rights", "Aboriginal Peoples' rights, voting rights and official language rights", "Employment rights, mobility rights, and freedom rights"),
            correctAnswerIndex = 1,
            explanation = "Mobility rights, Aboriginal peoples' rights, and official language rights are all distinct categories explicitly named in the Charter/Discover Canada, unlike \"property rights\" (option A) or \"voting rights\" replacing Aboriginal-specific wording (option C). (Discover Canada, page 8.)",
            topicGroupId = "q440"
        ),
        Question(
            id = 441,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "To what ocean is Newfoundland closest?",
            options = listOf("Atlantic", "Pacific", "Labrador Sea", "Arctic"),
            correctAnswerIndex = 0,
            explanation = "Newfoundland is on Canada's Atlantic coast (Labrador Sea is a body of water within the Atlantic but not one of the options meant to be \"the ocean\").",
            topicGroupId = "q441"
        ),
        Question(
            id = 442,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What UN operation did Canada participate in from 1950 to 1953?",
            options = listOf("Canadian Forces defended Hong Kong.", "The Canadian Corps captured Vimy Ridge.", "Canada participated in the UN operation defending South Korea in the Korean War.", "Canadians volunteered to fight in the South African War."),
            correctAnswerIndex = 2,
            explanation = "The Korean War (1950-1953) dates match exactly, and this was a UN-sanctioned operation Canada took part in. (Discover Canada, page 24.)",
            topicGroupId = "q442"
        ),
        Question(
            id = 443,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "From whom are the Acadians descended?",
            options = listOf("Métis and Inuit", "First Nations who began settling in what are now the Prairie provinces in 1600s", "British colonists who began settling in what are now the Maritime provinces in 1604", "French colonists who began settling in what are now the Maritime provinces in 1604"),
            correctAnswerIndex = 3,
            explanation = "Acadians descend from French settlers who founded Acadia in 1604 in the Maritimes. (Discover Canada, page 11.)",
            topicGroupId = "acadian_definition"
        ),
        Question(
            id = 444,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who has the right to enter and leave Canada at will?",
            options = listOf("Prisoners", "Members of the Commonwealth", "Canadian citizens", "Job seekers"),
            correctAnswerIndex = 2,
            explanation = "The right to enter, remain in, and leave Canada is a Charter right specific to Canadian citizens.",
            topicGroupId = "q444"
        ),
        Question(
            id = 445,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the Okanagan Valley famous for?",
            options = listOf("Coal Mines", "Lakes and fishing", "Fruit orchards", "Sunrise and sunset"),
            correctAnswerIndex = 2,
            explanation = "The Okanagan Valley is well known for fruit orchards and vineyards. (Discover Canada, page 49.)",
            topicGroupId = "q445"
        ),
        Question(
            id = 446,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "When did the name of \"Canada\" begin appearing on maps?",
            options = listOf("By the 1750s", "By the 1580s", "By the 1550s", "By the 1650s"),
            correctAnswerIndex = 2,
            explanation = "Discover Canada notes the name \"Canada\" was already appearing on maps by the 1550s. (Discover Canada, page 14.)",
            topicGroupId = "q446"
        ),
        Question(
            id = 447,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the difference between the role of the Sovereign and that of the Prime Minister?",
            options = listOf("The Sovereign links Canada to 52 other nations and the Prime Minister is the guardian of Constitutional freedoms.", "The Sovereign is the symbol of Canadian sovereignty and the Prime Minister is his aide.", "The Sovereign is Head of State, the Prime Minister oversees provincial policies.", "The Sovereign is the guardian of Constitutional freedoms, the Prime Minister selects the Cabinet Ministers and is responsible for operations and policy of government."),
            correctAnswerIndex = 3,
            explanation = "Discover Canada explains that the Sovereign is the guardian of constitutional freedoms while the Prime Minister selects the Cabinet and is responsible for the operations and policy of government. (Discover Canada, page 29.)",
            topicGroupId = "q447"
        ),
        Question(
            id = 448,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the meaning of the Remembrance Day poppy?",
            options = listOf("To remember our Sovereign, Queen Elizabeth II", "To remember the sacrifice of Canadians who have served or died in wars up to the present day", "To honour Prime Ministers who have died", "To celebrate Confederation"),
            correctAnswerIndex = 1,
            explanation = "The poppy's meaning as a symbol of remembrance for those who served/died in war is a core, unambiguous fact. (Discover Canada, page 52.)",
            topicGroupId = "q448"
        ),
        Question(
            id = 449,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who signs the bills if it is approved by the Provincial Parliament?",
            options = listOf("The Mayor", "The Premier", "The Members of the Provincial Parliament", "The Lieutenant-Governor"),
            correctAnswerIndex = 3,
            explanation = "The Lieutenant-Governor grants royal assent to provincial bills, analogous to the Governor General federally.",
            topicGroupId = "q449"
        ),
        Question(
            id = 450,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does M.P.P stand for?",
            options = listOf("Member of the Provincial Parachute", "Member of the Provincial Police", "Member of the Provincial Parliament", "Member of the Provincial Publication"),
            correctAnswerIndex = 2,
            explanation = "Other options are nonsensical distractors. (Discover Canada, page 29.)",
            topicGroupId = "q450"
        ),
        Question(
            id = 451,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What do you mark on a federal election ballot?",
            options = listOf("A checkmark", "An \"X\"", "A sticker", "A thumbprint"),
            correctAnswerIndex = 1,
            explanation = "Standard, well-known fact.",
            topicGroupId = "q451"
        ),
        Question(
            id = 452,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "When did thousands of miners first come to Yukon?",
            options = listOf("1870s", "1980s", "1780s", "1890s"),
            correctAnswerIndex = 3,
            explanation = "Klondike Gold Rush was 1896-1899.",
            topicGroupId = "q452"
        ),
        Question(
            id = 453,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the significance of the Canadian discovery of insulin?",
            options = listOf("It saved lives of children with sickness.", "It saved millions of lives of people with diabetes.", "It helped the treatment of heart diseases.", "It was an important medicine to save soldiers' life during World War II."),
            correctAnswerIndex = 1,
            explanation = "Banting and Best's discovery of insulin (1921/1922) is a well-known Canadian achievement highlighted in Discover Canada. (Discover Canada, page 27.)",
            topicGroupId = "q453"
        ),
        Question(
            id = 454,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What do you call the King's representative in the territories?",
            options = listOf("Commissioner", "Member of the Legislative Assembly", "\"Sir\"", "Lieutenant-Governor"),
            correctAnswerIndex = 0,
            explanation = "Territories have a Commissioner (distinct from provincial Lieutenant-Governors).",
            topicGroupId = "q454"
        ),
        Question(
            id = 455,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "From where does the name \"Canada\" come?",
            options = listOf("From the Inuit word \"Kanata\" meaning nations", "From \"Kanata\", the First Nations word for village", "From the Inuit word meaning home", "From the First Nations word meaning land"),
            correctAnswerIndex = 1,
            explanation = "Discover Canada states \"Canada\" derives from the Huron-Iroquois word \"kanata\" meaning village/settlement.",
            topicGroupId = "q455"
        ),
        Question(
            id = 456,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What are the three main types of industry in Canada?",
            options = listOf("Natural resources, manufacturing and services", "Mining, services and manufacturing", "Oil, tourism and manufacturing", "Fishery, tourism and services"),
            correctAnswerIndex = 0,
            explanation = "Matches Discover Canada's standard categorization of the economy. (Discover Canada, page 42.)",
            topicGroupId = "q456"
        ),
        Question(
            id = 457,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which country lies on Canada's southern border?",
            options = listOf("Central America", "Mexico", "Michigan", "United States of America"),
            correctAnswerIndex = 3,
            explanation = "Basic well-known fact.",
            topicGroupId = "q457"
        ),
        Question(
            id = 458,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What are the Prairie provinces?",
            options = listOf("Saskatchewan and Manitoba", "Alberta, Manitoba and British Columbia", "Saskatchewan, Alberta and Manitoba", "Saskatchewan and Alberta"),
            correctAnswerIndex = 2,
            explanation = "The three Prairie provinces are Manitoba, Saskatchewan, and Alberta. (Discover Canada, page 48.)",
            topicGroupId = "q458"
        ),
        Question(
            id = 459,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Where do English and French have equal status in Canada?",
            options = listOf("In the workplace", "In schools", "In the Parliament of Canada", "At the City Hall"),
            correctAnswerIndex = 2,
            explanation = "Discover Canada notes English and French have equal status in Parliament and throughout the federal government. (Discover Canada, page 8.)",
            topicGroupId = "q459"
        ),
        Question(
            id = 460,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is a majority government?",
            options = listOf("The party in power holds at least half of the seats in the House of Commons and the Senate.", "The party in power holds at least half of the seats in the House of Commons.", "The party in power holds less than half of the seats in the House of Commons.", "The party in power holds at least half of the seats in the Senate."),
            correctAnswerIndex = 1,
            explanation = "Majority government is defined by seats in the elected House of Commons, not the Senate. (Discover Canada, page 31.)",
            topicGroupId = "q460"
        ),
        Question(
            id = 461,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which two provinces are on the Atlantic coast of Canada?",
            options = listOf("British Columbia and Yukon", "Nova Scotia and New Brunswick", "Newfoundland and British Columbia", "Prince Edward Island and Ontario"),
            correctAnswerIndex = 1,
            explanation = "Given the answer choices - B is the only option pairing two genuine Atlantic-coast provinces (note PEI and Newfoundland are also Atlantic provinces but aren't paired correctly in the other options).",
            topicGroupId = "q461"
        ),
        Question(
            id = 462,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following statements about residential schools is NOT true?",
            flashcardText = "Which statement about residential schools is NOT true?",
            options = listOf("The federal government placed many Aboriginal children in residential schools to educate and assimilate them into mainstream Canadian culture.", "The schools were poorly funded and inflicted hardship on the students.", "The schools were welcomed by the Aboriginal people.", "Aboriginal language and cultural practices were mostly prohibited."),
            correctAnswerIndex = 2,
            explanation = "Residential schools were widely resisted/harmful, not welcomed; this is the clearly false statement.",
            topicGroupId = "q462"
        ),
        Question(
            id = 463,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who have major responsibilities on First Nations reserves?",
            options = listOf("Band Chiefs and councillors", "Municipal governments", "Provincial and territorial governments", "Federal government"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada notes elected chiefs and councils have local governance responsibilities on reserves. (Discover Canada, page 33.)",
            topicGroupId = "q463"
        ),
        Question(
            id = 464,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who are exempted from the requirement of adequate knowledge of English or French, in order to become a Canadian citizen?",
            options = listOf("Anyone who doesn't live in major city", "Any adult applicants who are 55 years of age and under", "Any adult applicants who are 55 years of age and over", "No one"),
            correctAnswerIndex = 2,
            explanation = "Current citizenship rules exempt applicants aged 55 and over (and under 18) from the language/knowledge test requirement. (Discover Canada, page 39.)",
            topicGroupId = "q464"
        ),
        Question(
            id = 465,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who played a key role in defending Canada during the War of 1812, and led a group of Shawnee warriors in support of British soldiers and Canadian volunteers?",
            options = listOf("Major-General Sir Isaac Brock", "Lieutenant-Colonel Charles de Salaberry", "Chief Tecumseh", "Major-General Robert Ross"),
            correctAnswerIndex = 2,
            explanation = "Chief Tecumseh led Shawnee and other First Nations warriors allied with the British in the War of 1812. (Discover Canada, page 17.)",
            topicGroupId = "q465"
        ),
        Question(
            id = 466,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does Confederation mean?",
            options = listOf("The joining of provinces to become a new country", "The United States Confederate army came to settle in Canada.", "The combination of neighborhood to build a larger community", "The merger of colonies to form a province"),
            correctAnswerIndex = 0,
            explanation = "Standard definition of Confederation (1867). (Discover Canada, page 53.)",
            topicGroupId = "q466"
        ),
        Question(
            id = 467,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In what year were the Aboriginal Peoples granted the right to vote?",
            options = listOf("1960", "1790", "1950", "1632"),
            correctAnswerIndex = 0,
            explanation = "Status Indians received the unconditional right to vote in federal elections in 1960. (Discover Canada, page 25.)",
            topicGroupId = "q467"
        ),
        Question(
            id = 468,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In which period did Canada's economy and industry experience a boom?",
            options = listOf("1880s", "1890s and early 1900s", "1920s", "1860s"),
            correctAnswerIndex = 1,
            explanation = "Discover Canada describes the Laurier-era wheat and industrial boom of the 1890s-early 1900s. (Discover Canada, page 20.)",
            topicGroupId = "q468"
        ),
        Question(
            id = 469,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which two are Great Lakes?",
            options = listOf("St. Lawrence and Superior", "Ontario and Okanagan", "Michigan and Okanagan", "Huron and Erie"),
            correctAnswerIndex = 3,
            explanation = "Huron and Erie are both genuine Great Lakes; the other options pair a real lake with a non-Great Lake (St. Lawrence is a river, Okanagan is a BC lake).",
            topicGroupId = "q469"
        ),
        Question(
            id = 470,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is known as the effort by women to achieve the right to vote?",
            options = listOf("The suffrage motion of women", "The women's voting law", "The election law", "The women's suffrage movement"),
            correctAnswerIndex = 3,
            explanation = "Standard terminology. (Discover Canada, page 21.)",
            topicGroupId = "q470"
        ),
        Question(
            id = 471,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What do political parties do?",
            options = listOf("Follow commands from the King", "Share ideas about how government should work", "Plan for the celebration of Canada Day", "Work with the local governments"),
            correctAnswerIndex = 1,
            explanation = "Matches Discover Canada's description of political parties presenting different ideas/policies.",
            topicGroupId = "q471"
        ),
        Question(
            id = 472,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who were the United Empire Loyalists?",
            options = listOf("Inuit and First Nations", "French and British settlers", "First Nations and British settlers", "Settlers from the United States during the American Revolution"),
            correctAnswerIndex = 3,
            explanation = "United Empire Loyalists were American colonists loyal to the Crown who fled to Canada during/after the American Revolution.",
            topicGroupId = "united_empire_loyalists_group"
        ),
        Question(
            id = 473,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which province is Canada's largest producer of hydroelectricity?",
            options = listOf("British Columbia", "Manitoba", "Ontario", "Québec"),
            correctAnswerIndex = 3,
            explanation = "Québec is Canada's largest hydroelectricity producer (Hydro-Québec). (Discover Canada, page 47.)",
            topicGroupId = "q473"
        ),
        Question(
            id = 474,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which territory shares a border with another country?",
            options = listOf("British Columbia", "Alberta", "Northwest Territories", "Yukon Territory"),
            correctAnswerIndex = 3,
            explanation = "Yukon borders Alaska, USA.",
            topicGroupId = "q474"
        ),
        Question(
            id = 475,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who are the Quebecers?",
            options = listOf("European settlers in the 1600s", "Descendants of the French colonists", "Descendants of the Anglophones", "People of Québec"),
            correctAnswerIndex = 3,
            explanation = "Discover Canada states: 'Quebecers are the people of Quebec, the vast majority French-speaking.' (Discover Canada, page 11.)",
            topicGroupId = "q475"
        ),
        Question(
            id = 476,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Why is British Columbia known as Canada's Pacific Gateway?",
            options = listOf("Because billions of dollars in goods are shipped to and from Asia", "Because it has Pacific Ocean on its coastline", "Because many people of Asian origin live there", "Because it attracts many tourists all year round"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada describes BC's ports as a trade gateway with Asia-Pacific, moving billions of dollars in goods, distinct from just coastline geography. (Discover Canada, page 49.)",
            topicGroupId = "q476"
        ),
        Question(
            id = 477,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "When was the Magna Carta signed?",
            options = listOf("1649", "1215", "1425", "1615"),
            correctAnswerIndex = 1,
            explanation = "The Magna Carta was signed in 1215.",
            topicGroupId = "q477"
        ),
        Question(
            id = 478,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does the Great Charter of Freedom include?",
            options = listOf("Aboriginal Peoples' rights", "Employment rights", "Freedom of conscience and religion", "Freedom from taxes"),
            correctAnswerIndex = 2,
            explanation = "\"Great Charter of Freedom\" is an unusual phrasing (adjacent to the Magna Carta question) but \"freedom of conscience and religion\" is a verbatim fundamental freedom listed in the Charter of Rights and Freedoms in Discover Canada, and is the only option matching guide content; the other options do not fit either charter. (Discover Canada, page 8.)",
            topicGroupId = "q478"
        ),
        Question(
            id = 479,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is \"Habeas corpus\"?",
            options = listOf("The right to live and work anywhere in Canada", "The right for peaceful assembly", "The right to speak freely", "The right to challenge unlawful detention by the state"),
            correctAnswerIndex = 3,
            explanation = "This is the exact definition given in Discover Canada. (Discover Canada, page 8.)",
            topicGroupId = "q479"
        ),
        Question(
            id = 480,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who invented the snowmobile?",
            options = listOf("Alexander Graham Bell", "Joseph-Armand Bombardier", "Sir Sandford Fleming", "Mathew Evans and Henry Woodward"),
            correctAnswerIndex = 1,
            explanation = "Bombardier is credited with inventing the snowmobile in Discover Canada. (Discover Canada, page 27.)",
            topicGroupId = "q480"
        ),
        Question(
            id = 481,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Under Canadian law, why is every person presumed to be innocent until proven guilty?",
            options = listOf("No person or group is above the law.", "Men and women are equal under the law.", "Freedom of thought, belief, opinion, and expression", "To guarantee the due legal process under the law"),
            correctAnswerIndex = 3,
            explanation = "Presumption of innocence is presented in Discover Canada as part of the guarantee of due process under the law; ruled out the rule-of-law and equality options as those answer different questions. (Discover Canada, page 36.)",
            topicGroupId = "q481"
        ),
        Question(
            id = 482,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who governs Canada on a daily basis at the federal level?",
            options = listOf("The Premier", "The Governor General", "The King", "The Prime Minister"),
            correctAnswerIndex = 3,
            explanation = "Standard fact, the Prime Minister runs the day-to-day government while the King/Governor General are ceremonial heads of state. (Discover Canada, page 53.)",
            topicGroupId = "pm_head_of_government"
        ),
        Question(
            id = 483,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who is awarded the honour of Victoria Cross?",
            options = listOf("Canadian politicians", "Police officers", "Best Innovation of the year", "A Canadian showing conspicuous bravery or self sacrifice"),
            correctAnswerIndex = 3,
            explanation = "Matches the standard description of the Victoria Cross as Canada's highest military honour for gallantry. (Discover Canada, page 41.)",
            topicGroupId = "q483"
        ),
        Question(
            id = 484,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Federal elections are carried out to elect:",
            options = listOf("the Premier", "the Prime Minister", "the Member of Parliament", "the Senator"),
            correctAnswerIndex = 2,
            explanation = "Voters directly elect their local MP; the Prime Minister is not directly elected in a federal election (common civics distinction tested in Discover Canada).",
            topicGroupId = "q484"
        ),
        Question(
            id = 485,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is a part of our heritage under the Canadian legal system?",
            options = listOf("Freedom under law", "Democratic principles and due process", "Rule of law", "All of the above"),
            correctAnswerIndex = 3,
            explanation = "Discover Canada lists freedom under law, democratic principles/due process, and rule of law together as part of Canada's legal heritage, so \"all of the above\" fits.",
            topicGroupId = "q485"
        ),
        Question(
            id = 486,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Where do Inuit people live?",
            options = listOf("Ontario", "Reserve land", "In scattered communities across the Arctic", "Prairie Provinces"),
            correctAnswerIndex = 2,
            explanation = "Matches Discover Canada's description of Inuit settlement in the Arctic. (Discover Canada, page 11.)",
            topicGroupId = "q486"
        ),
        Question(
            id = 487,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who contributed to the invention of the radio and also sent the world's first wireless voice message?",
            options = listOf("Reginald Fessenden", "Alexander Graham Bell", "Mike Lazaridis", "Mathew Evans"),
            correctAnswerIndex = 0,
            explanation = "Fessenden is credited in Discover Canada with early radio development and the first wireless voice transmission. (Discover Canada, page 27.)",
            topicGroupId = "q487"
        ),
        Question(
            id = 488,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which region was stormed and captured on D-Day (June 6, 1944) by the Canadian troops?",
            options = listOf("Berlin", "Juno Beach", "London", "Paris"),
            correctAnswerIndex = 1,
            explanation = "Juno Beach is the well-documented Canadian D-Day objective, explicitly named in Discover Canada. (Discover Canada, page 23.)",
            topicGroupId = "q488"
        ),
        Question(
            id = 489,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who invented the sport of basketball?",
            options = listOf("Canadians", "French", "Germans", "Americans"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada credits Canadian James Naismith as the inventor of basketball.",
            topicGroupId = "q489"
        ),
        Question(
            id = 490,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What are the men who established Canada called?",
            options = listOf("Fathers of confederation", "Fathers of Dominion of Canada", "Fathers of Canada", "Fathers of Constitution"),
            correctAnswerIndex = 0,
            explanation = "\"Fathers of Confederation\" is the standard term used throughout Discover Canada. (Discover Canada, page 18.)",
            topicGroupId = "q490"
        ),
        Question(
            id = 491,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In which year was the British Parliament prohibited from buying and selling slaves?",
            options = listOf("1793", "1877", "1807", "1833"),
            correctAnswerIndex = 2,
            explanation = "1807 is the year of the British Slave Trade Act, cited in Discover Canada (full abolition of slavery followed in 1833/1834). (Discover Canada, page 16.)",
            topicGroupId = "q491"
        ),
        Question(
            id = 492,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who became the first French-Canadian Prime Minister since the formation of Confederation?",
            options = listOf("Sir John Alexander Macdonald", "Sir Wilfrid Laurier", "Sir George-Étienne Cartier", "Sir Leonard Tilley"),
            correctAnswerIndex = 1,
            explanation = "Laurier (elected 1896) is well documented in Discover Canada as the first French-Canadian PM. (Discover Canada, page 20.)",
            topicGroupId = "q492"
        ),
        Question(
            id = 493,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who represents an electoral district?",
            options = listOf("The Commissioner", "The Governor General", "The Lieutenant Governor", "A Member of Parliament"),
            correctAnswerIndex = 3,
            explanation = "MPs represent electoral districts (ridings) in the House of Commons. (Discover Canada, page 30.)",
            topicGroupId = "q493"
        ),
        Question(
            id = 494,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following was invented by Alexander Graham Bell?",
            flashcardText = "What did Alexander Graham Bell invent?",
            options = listOf("BlackBerry", "Fax Machine", "Telephone", "Internet"),
            correctAnswerIndex = 2,
            explanation = "Well-known historical fact, also stated in Discover Canada. (Discover Canada, page 27.)",
            topicGroupId = "q494"
        ),
        Question(
            id = 495,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is celebrated on April 9th?",
            options = listOf("Vimy Day", "Family Day", "Thanksgiving Day", "Boxing Day"),
            correctAnswerIndex = 0,
            explanation = "April 9 is Vimy Ridge Day, commemorating the Battle of Vimy Ridge (April 9, 1917), covered in Discover Canada.",
            topicGroupId = "q495"
        ),
        Question(
            id = 496,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "When was \"employment insurance\" introduced by the Canadian federal government?",
            options = listOf("1947", "1950", "1940", "1965"),
            correctAnswerIndex = 2,
            explanation = "The federal Unemployment Insurance Act was enacted in 1940, as referenced in Discover Canada. (Discover Canada, page 24.)",
            topicGroupId = "q496"
        ),
        Question(
            id = 497,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What information can be found on a voter information card?",
            options = listOf("Confirms that your name is on the voters' list", "States when you vote", "States where you vote", "All of the above"),
            correctAnswerIndex = 3,
            explanation = "Voter information cards contain all three pieces of information listed. (Discover Canada, page 30.)",
            topicGroupId = "voter_information_card_definition"
        ),
        Question(
            id = 498,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is \"Due process\"?",
            options = listOf("The government must respect all of the legal rights a person is entitled to under the law.", "The rule of law and freedom under the law", "The impartial manner in which the laws are administered", "None of these"),
            correctAnswerIndex = 0,
            explanation = "This closely matches Discover Canada's definition of due process. (Discover Canada, page 36.)",
            topicGroupId = "q498"
        ),
        Question(
            id = 499,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who among the following can help you with legal problems?",
            options = listOf("Politicians", "Lawyers", "Members of Parliament", "The police"),
            correctAnswerIndex = 1,
            explanation = "Lawyers are the standard answer for legal assistance. (Discover Canada, page 37.)",
            topicGroupId = "q499"
        ),
        Question(
            id = 500,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "To whom do we profess our loyalty in Canada?",
            options = listOf("The Canadian Flag", "A person who represents all Canadians", "Geopolitical entities", "The Canadian Constitution"),
            correctAnswerIndex = 1,
            explanation = "Discover Canada explains that Canadians' allegiance to the King/Queen is loyalty to a person who symbolizes/represents all Canadians, rather than to any individual government or party. (Discover Canada, page 2.)",
            topicGroupId = "q500"
        ),
        Question(
            id = 501,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the last line of our National Anthem?",
            options = listOf("God keep our land glorious and free!", "The true North strong and free!", "O Canada! Our home and native land!", "O Canada, we stand on guard for thee!"),
            correctAnswerIndex = 3,
            explanation = "\"O Canada, we stand on guard for thee\" is the final line of the anthem's official lyrics. (Discover Canada, page 40.)",
            topicGroupId = "q501"
        ),
        Question(
            id = 502,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What are the members of the House of Commons also known as?",
            options = listOf("Commissioners", "Members of Parliament or MPs", "Members of the Provincial Parliament", "None of these"),
            correctAnswerIndex = 1,
            explanation = "Standard terminology. (Discover Canada, page 30.)",
            topicGroupId = "mp_initials_meaning"
        ),
        Question(
            id = 503,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What did it mean for someone to be called a 'Loyalist' during the American Revolution?",
            options = listOf("People loyal to the Crown", "The commanders of armies", "British colonies", "Aboriginal peoples"),
            correctAnswerIndex = 0,
            explanation = "Matches Discover Canada's description of Loyalists who fled to Canada after the American Revolution. (Discover Canada, page 15.)",
            topicGroupId = "united_empire_loyalists_group"
        ),
        Question(
            id = 504,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following governments provides \"publicly-funded\" education?",
            flashcardText = "Which level of government provides \"publicly-funded\" education?",
            options = listOf("City government", "Federal government", "Provincial and territorial governments", "None of these"),
            correctAnswerIndex = 2,
            explanation = "Education funding is a provincial/territorial responsibility per Discover Canada. (Discover Canada, page 33.)",
            topicGroupId = "provincial_responsibility_education"
        ),
        Question(
            id = 505,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "The first leader of a responsible government in Canada in 1849 was:",
            options = listOf("Louis Riel", "Sir John Alexander Macdonald", "Sir Louis-Hippolyte La Fontaine", "Robert Baldwin"),
            correctAnswerIndex = 2,
            explanation = "La Fontaine (co-premier with Robert Baldwin) led the first responsible government of the united Province of Canada starting 1848/1849; \"Alec Baldwin\" is clearly a joke distractor for Robert Baldwin, who is not an available option, making La Fontaine the best answer among these choices. (Discover Canada, page 18.)",
            topicGroupId = "la_fontaine_first_responsible_government"
        ),
        Question(
            id = 506,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who are Francophones?",
            options = listOf("People speaking French as a first language", "People who come from France", "People who are learning French", "People speaking French as a secondary language"),
            correctAnswerIndex = 0,
            explanation = "Standard Discover Canada definition. (Discover Canada, page 11.)",
            topicGroupId = "q506"
        ),
        Question(
            id = 507,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How many Canadians served in World War II?",
            options = listOf("More than one million", "Less than 500,000", "About 900,000", "About 500,000"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada states more than one million Canadians served in WWII. (Discover Canada, page 23.)",
            topicGroupId = "q507"
        ),
        Question(
            id = 508,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What language do more than three-quarters of the people who live in Québec speak?",
            options = listOf("French as their second language", "French as their first language", "German as their first language", "English as their first language"),
            correctAnswerIndex = 1,
            explanation = "Matches Discover Canada's demographic description of Quebec. (Discover Canada, page 47.)",
            topicGroupId = "q508"
        ),
        Question(
            id = 509,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the national police force of Canada?",
            options = listOf("The Royal Canadian Mounted Police", "The South East Mounted Police", "The Military Police", "The North West Mounted Police"),
            correctAnswerIndex = 0,
            explanation = "The RCMP is Canada's national police force (successor to the North-West Mounted Police, a distractor option here). (Discover Canada, page 19.)",
            topicGroupId = "q509"
        ),
        Question(
            id = 510,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "The name \"Canada\" became the official name of the country in the year:",
            options = listOf("1799", "1773", "1791", "1867"),
            correctAnswerIndex = 2,
            explanation = "1867 is the year of Confederation, but the name \"Canada\" became official earlier. Discover Canada states: 'The Constitutional Act of 1791 divided the Province of Quebec into Upper Canada (later Ontario)... and Lower Canada (later Quebec)... The name Canada also became official at this time and has been used ever since.' (Discover Canada, page 16.)",
            topicGroupId = "q510"
        ),
        Question(
            id = 511,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who was Sir Sam Steele?",
            options = listOf("A great frontier hero, Mounted Policeman, and soldier of the Queen", "A military leader of the Métis in the 19th century", "The first Prime Minister of Canada", "The Father of Manitoba"),
            correctAnswerIndex = 0,
            explanation = "Option A is the exact phrasing used in Discover Canada to describe Sir Sam Steele. (Discover Canada, page 19.)",
            topicGroupId = "q511"
        ),
        Question(
            id = 512,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "The largest religious affiliation in Canada is:",
            options = listOf("Roman Catholic", "Hindu", "Muslim", "Jewish"),
            correctAnswerIndex = 0,
            explanation = "Roman Catholicism is the largest religious affiliation in Canada per Discover Canada. (Discover Canada, page 13.)",
            topicGroupId = "q512"
        ),
        Question(
            id = 513,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who suggested the name Dominion of Canada in 1864?",
            options = listOf("Sir Leonard Tilley", "Lord Elgin", "La Fontaine", "Sir John Alexander Macdonald"),
            correctAnswerIndex = 0,
            explanation = "Sir Leonard Tilley of New Brunswick proposed \"Dominion of Canada,\" inspired by Psalm 72:8. (Discover Canada, page 18.)",
            topicGroupId = "q513"
        ),
        Question(
            id = 514,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What was significant about the Canadian navy at the end of the Second World War?",
            options = listOf("It was the third-largest navy in the world.", "It was the fourth-largest navy in the world.", "It was the largest navy in the world.", "It was the second-largest navy in the world."),
            correctAnswerIndex = 0,
            explanation = "Discover Canada states: 'At the end of the Second World War, Canada had the third largest navy in the world.' (Discover Canada, page 23.)",
            topicGroupId = "q514"
        ),
        Question(
            id = 515,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which is the most famous invention of Research In Motion (RIM), a wireless communications company?",
            options = listOf("The Canadarm", "The first wireless voice message", "The BlackBerry", "The iPhone"),
            correctAnswerIndex = 2,
            explanation = "RIM's most famous invention is the BlackBerry smartphone. (Discover Canada, page 27.)",
            topicGroupId = "q515"
        ),
        Question(
            id = 516,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What are the Métis people a mixture of?",
            options = listOf("Aboriginal and European ancestry", "European and American ancestry", "American and Indian ancestry", "Inuit and Indian ancestry"),
            correctAnswerIndex = 0,
            explanation = "Métis are defined as people of mixed Aboriginal and European ancestry. (Discover Canada, page 11.)",
            topicGroupId = "q516"
        ),
        Question(
            id = 517,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In 1996, at the Olympic Games, which Canadian became a world record sprinter and double Olympic gold medalist?",
            options = listOf("Wayne Gretzky", "La Fontaine", "Donovan Bailey", "John Cabot"),
            correctAnswerIndex = 2,
            explanation = "Donovan Bailey won double gold and set a world record in the 100m at the 1996 Atlanta Olympics. (Discover Canada, page 26.)",
            topicGroupId = "q517"
        ),
        Question(
            id = 518,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which was the first province in the Empire to move towards the abolition of slavery?",
            options = listOf("South Canada", "Upper Canada", "North America", "Lower Canada"),
            correctAnswerIndex = 1,
            explanation = "Upper Canada passed an act in 1793 starting the gradual abolition of slavery, a first in the British Empire. (Discover Canada, page 16.)",
            topicGroupId = "q518"
        ),
        Question(
            id = 519,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following lists contains four rights that Canadians have?",
            flashcardText = "What are four rights that Canadians have?",
            options = listOf("The right to go to school, to work, to have a bank account, and to travel", "The right to be educated in either official language, to vote, to apply for a Canadian passport, and to enter and leave Canada freely", "The right to travel, to live anywhere, to work anywhere, and to get married", "The right to have a job, to vote, to drive, and to go to school"),
            correctAnswerIndex = 1,
            explanation = "Option B matches the specific list of rights (mobility, language, voting, passport) enumerated in Discover Canada. (Discover Canada, page 8.)",
            topicGroupId = "q519"
        ),
        Question(
            id = 520,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which province is connected to mainland Canada by one of the longest continuous multispan bridges in the world?",
            options = listOf("Prince Edward Island", "Newfoundland and Labrador", "Alberta", "Ontario"),
            correctAnswerIndex = 0,
            explanation = "The Confederation Bridge links PEI to mainland New Brunswick and is one of the world's longest multispan bridges over ice-covered water. (Discover Canada, page 46.)",
            topicGroupId = "q520"
        ),
        Question(
            id = 521,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who invented the cardiac pacemaker?",
            options = listOf("Gabriel Dumont", "Mathew Evans and Henry Woodward", "Dr. John A. Hopps", "Alexander Graham Bell"),
            correctAnswerIndex = 2,
            explanation = "Dr. John A. Hopps, a Canadian, is credited as inventor of the cardiac pacemaker. (Discover Canada, page 27.)",
            topicGroupId = "q521"
        ),
        Question(
            id = 522,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is significant about the number of people living in Ontario?",
            options = listOf("They make up two-thirds of all Canadians.", "They make up three-fourths of all Canadians.", "They make up one-third of all Canadians.", "They make up half of all Canadians."),
            correctAnswerIndex = 2,
            explanation = "Discover Canada notes Ontario is home to more than a third of Canada's population.",
            topicGroupId = "ontario_population_third_of_canada"
        ),
        Question(
            id = 523,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "When asked, who must you tell who you voted for in a federal election?",
            options = listOf("A police officer", "Your employer", "An Elections Canada official", "No one"),
            correctAnswerIndex = 3,
            explanation = "Canada uses a secret ballot; voters are not required to disclose their choice to anyone. (Discover Canada, page 31.)",
            topicGroupId = "secret_ballot_no_disclosure"
        ),
        Question(
            id = 524,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which province has a long history of coal mining, forestry, and agriculture?",
            options = listOf("Nova Scotia", "New Brunswick", "Prince Edward Island", "Ontario"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada describes Nova Scotia's economy in these terms. (Discover Canada, page 46.)",
            topicGroupId = "q524"
        ),
        Question(
            id = 525,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who were the first people (Aboriginal People) living in Canada?",
            options = listOf("American and British", "Spanish", "French and Chinese", "First Nations and Inuits"),
            correctAnswerIndex = 3,
            explanation = "First Nations and Inuit peoples were among the earliest inhabitants of Canada.",
            topicGroupId = "q525"
        ),
        Question(
            id = 526,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "If you are unable to vote on election day, how do you vote?",
            options = listOf("Vote at advance polls", "Forget it", "Vote the next day after the election", "Vote a week later"),
            correctAnswerIndex = 0,
            explanation = "Advance polls allow voting before election day. (Discover Canada, page 32.)",
            topicGroupId = "q526"
        ),
        Question(
            id = 527,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the first line of the Canadian national anthem?",
            options = listOf("O Canada! Our home and native land!", "O Canada! Land of our ancestors", "O Canada! We stand on guard for thee", "O Canada! Glorious and free"),
            correctAnswerIndex = 0,
            explanation = "Correct and well-known first line of \"O Canada.\" (Discover Canada, page 40.)",
            topicGroupId = "q527"
        ),
        Question(
            id = 528,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the meaning of the phrase \"the world's longest undefended border\"?",
            options = listOf("Canada exports billions of dollars' worth of energy products to the USA.", "Canada enjoys close relations with the United States.", "Over three-quarters of Canadian exports are destined for the USA.", "Millions of Canadians and Americans cross the border every year in safety."),
            correctAnswerIndex = 3,
            explanation = "This phrasing closely mirrors Discover Canada's description of the undefended border. (Discover Canada, page 43.)",
            topicGroupId = "q528"
        ),
        Question(
            id = 529,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "When was the first representative assembly in Canada elected?",
            options = listOf("1791", "1758", "1889", "1609"),
            correctAnswerIndex = 1,
            explanation = "The first representative assembly in Canada met in Halifax, Nova Scotia in 1758. (Discover Canada, page 16.)",
            topicGroupId = "q529"
        ),
        Question(
            id = 530,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following are the provinces responsible for?",
            flashcardText = "What are the provinces responsible for?",
            options = listOf("Defence", "Foreign Policy", "Currency", "Education"),
            correctAnswerIndex = 3,
            explanation = "Education is a provincial responsibility, unlike defence, foreign policy, and currency which are federal.",
            topicGroupId = "provincial_responsibility_education"
        ),
        Question(
            id = 531,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "When is Labour Day celebrated in Canada?",
            options = listOf("The 1st of July", "The 1st Monday of September", "The 1st of May", "The 3rd Monday of October"),
            correctAnswerIndex = 1,
            explanation = "Labour Day in Canada falls on the first Monday of September.",
            topicGroupId = "q531"
        ),
        Question(
            id = 532,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What did the government do to make immigration to Western Canada easier?",
            options = listOf("Use the Great Lakes and seaway to prairies", "Built a railway across the prairies", "Built a highway across the prairies", "A and C"),
            correctAnswerIndex = 1,
            explanation = "The transcontinental railway (CPR) was built to open the West to settlement.",
            topicGroupId = "q532"
        ),
        Question(
            id = 533,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which trade spread across Canada making it important to the economy for over 300 years?",
            options = listOf("Beaver fur trade", "Fisheries", "Lumber", "Gold"),
            correctAnswerIndex = 0,
            explanation = "The beaver fur trade was central to the Canadian economy for over 300 years.",
            topicGroupId = "q533"
        ),
        Question(
            id = 534,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does the Governor General perform?",
            options = listOf("After an election, he or she invites the party with the most votes to form the new government.", "Signs bills to make them law", "All of the above", "None of the above"),
            correctAnswerIndex = 2,
            explanation = "Both duties listed (A and B) are genuine functions of the Governor General, so \"All of the above\" is correct.",
            topicGroupId = "q534"
        ),
        Question(
            id = 535,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "When was the Official Languages Act passed?",
            options = listOf("1969", "1867", "1982", "2000"),
            correctAnswerIndex = 0,
            explanation = "The Official Languages Act was passed in 1969. (Discover Canada, page 39.)",
            topicGroupId = "q535"
        ),
        Question(
            id = 536,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does B.N.A. stand for?",
            options = listOf("British National Alliance", "British North America Act", "Black Nation Alliance", "Bank of National Association"),
            correctAnswerIndex = 1,
            explanation = "B.N.A. = British North America Act, the founding constitutional document of 1867. (Discover Canada, page 18.)",
            topicGroupId = "q536"
        ),
        Question(
            id = 537,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How many levels of government are there in Canada?",
            options = listOf("Ten", "Five", "Thirteen", "Three"),
            correctAnswerIndex = 3,
            explanation = "Canada has three levels of government: federal, provincial/territorial, and municipal.",
            topicGroupId = "q537"
        ),
        Question(
            id = 538,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How are laws passed?",
            options = listOf("Signed by the Governor General", "Read by the House of Commons three times", "Read by the Senate three times", "All of the above"),
            correctAnswerIndex = 3,
            explanation = "A bill must pass three readings in both the House of Commons and Senate, then receive Royal Assent (signed by the Governor General).",
            topicGroupId = "q538"
        ),
        Question(
            id = 539,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who signs the bills to make them law?",
            options = listOf("The Police Chief", "The Governor General", "The Premier", "The Prime Minister"),
            correctAnswerIndex = 1,
            explanation = "The Governor General gives Royal Assent, signing bills into law. (Discover Canada, page 29.)",
            topicGroupId = "q539"
        ),
        Question(
            id = 540,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is a noble way to contribute to Canada and an excellent career choice?",
            options = listOf("Serve in the regular Canadian Forces", "Serve on a jury", "Belong to a union", "Learn both official languages"),
            correctAnswerIndex = 0,
            explanation = "Discover Canada describes serving in the Canadian Forces in this exact phrasing. (Discover Canada, page 9.)",
            topicGroupId = "q540"
        ),
        Question(
            id = 541,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which province is Canada's leading wheat producer?",
            options = listOf("Manitoba", "New Brunswick", "Alberta", "Saskatchewan"),
            correctAnswerIndex = 3,
            explanation = "Saskatchewan is Canada's leading wheat-producing province.",
            topicGroupId = "q541"
        ),
        Question(
            id = 542,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does a Member of Parliament do?",
            options = listOf("He or she links Canadians to the federal government.", "He or she represents the King.", "He or she works for the Governor General.", "He or she liaises with the municipal government."),
            correctAnswerIndex = 0,
            explanation = "An MP represents constituents and links them to the federal government. (Discover Canada, page 30.)",
            topicGroupId = "q542"
        ),
        Question(
            id = 543,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following are Canada's famous writers?",
            flashcardText = "Who are some of Canada's famous writers?",
            options = listOf("Sir Ernest MacMillan and Healey Willan", "Paul Henderson and Mark Tewksbury", "Joy Kogawa, Michael Ondaatje, and Rohinton Mistry", "Emily Carr and Louis-Philippe Hébert"),
            correctAnswerIndex = 2,
            explanation = "Kogawa, Ondaatje, and Mistry are well-known Canadian authors listed in Discover Canada; the other options are composers, athletes, and visual artists/sculptors. (Discover Canada, page 25.)",
            topicGroupId = "q543"
        ),
        Question(
            id = 544,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Sir Louis-Hippolyte La Fontaine was known for:",
            options = listOf("A champion of democracy and Aboriginal rights", "A champion of democracy and French language rights and the first leader of a responsible government in the Canadas", "The first Head of State", "The first French-speaking Prime Minister"),
            correctAnswerIndex = 1,
            explanation = "A champion of democracy and French language rights, La Fontaine became the first leader of a responsible government in the Canadas. (Discover Canada, page 18.)",
            topicGroupId = "la_fontaine_first_responsible_government"
        ),
        Question(
            id = 545,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which port is the largest and busiest in Canada?",
            options = listOf("The Port of Halifax", "The Port of Montreal", "The Port of Vancouver", "The Port of Victoria"),
            correctAnswerIndex = 2,
            explanation = "The Port of Vancouver, Canada's largest and busiest, handles billions of dollars in goods traded around the world. (Discover Canada, page 49.)",
            topicGroupId = "q545"
        ),
        Question(
            id = 546,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What did the Canadian Pacific Railway symbolize?",
            options = listOf("Easy access to the West Coast", "What can be achieved by working together", "Unity", "Ribbons of steel"),
            correctAnswerIndex = 2,
            explanation = "The completion of the Canadian Pacific Railway in 1885, driving the last spike at Craigellachie, was a powerful symbol of national unity. (Discover Canada, page 20.)",
            topicGroupId = "q546"
        ),
        Question(
            id = 547,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What part of the Constitution legally protects basic rights and freedom of Canadians?",
            options = listOf("The Canada Charter of Responsibilities", "The Charter of Rights and Freedoms", "The Canadian Charter of Rights and Freedoms", "The Canadian Charter of Rights and Free Will"),
            correctAnswerIndex = 2,
            explanation = "The Canadian Charter of Rights and Freedoms is the part of the Constitution that legally protects the basic rights and freedoms of Canadians. (Discover Canada, page 8.)",
            topicGroupId = "q547"
        ),
        Question(
            id = 548,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How many votes can a voter have in a federal election?",
            options = listOf("It does not matter", "Three", "Two", "One"),
            correctAnswerIndex = 3,
            explanation = "Each voter gets exactly one vote in a federal election. (Discover Canada, page 32.)",
            topicGroupId = "q548"
        ),
        Question(
            id = 549,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How much of Canadian exports are destined for the USA?",
            options = listOf("Over one-third", "Over three-quarters", "Two-thirds", "Half"),
            correctAnswerIndex = 1,
            explanation = "More than three-quarters of Canada's exports are destined for the United States, its largest trading partner. (Discover Canada, page 43.)",
            topicGroupId = "q549"
        ),
        Question(
            id = 550,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Where do you go to vote?",
            options = listOf("Polling station", "City hall", "Police station", "Fire station"),
            correctAnswerIndex = 0,
            explanation = "On election day, voters cast their ballot at a polling station. (Discover Canada, page 30.)",
            topicGroupId = "polling_station_definition"
        ),
        Question(
            id = 551,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "As what have poets and songwriters hailed Canada?",
            options = listOf("\"Peace, Order, and Good Government\"", "The \"Great Outdoors\"", "The \"Great Dominion\"", "The \"Land of the Brave\""),
            correctAnswerIndex = 2,
            explanation = "\"The Great Dominion\" is how poets and songwriters have hailed Canada. (Discover Canada, page 10.)",
            topicGroupId = "q551"
        ),
        Question(
            id = 552,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Where did the early European settlers live?",
            options = listOf("Western Canada", "North West Canada", "Northern Canada", "Eastern and Central Canada"),
            correctAnswerIndex = 3,
            explanation = "Early European settlement in Canada was concentrated in the eastern and central regions of the country.",
            topicGroupId = "q552"
        ),
        Question(
            id = 553,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Canadians work hard to respect:",
            options = listOf("Marxism", "Pluralism", "Capitalism", "Individualism"),
            correctAnswerIndex = 1,
            explanation = "Canadians work hard to respect pluralism and live together in harmony and mutual respect. (Discover Canada, page 8.)",
            topicGroupId = "q553"
        ),
        Question(
            id = 554,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following was a key phrase in the British North America Act, Canada's original constitutional document in 1867?",
            flashcardText = "What was a key phrase in the British North America Act, Canada's original constitutional document in 1867?",
            options = listOf("Geopolitical entity", "Trade and communications", "Peace, Order, and Good Government", "Discipline, education, and good public"),
            correctAnswerIndex = 2,
            explanation = "\"Peace, Order, and Good Government\" was the key phrase in the British North America Act of 1867, Canada's original constitutional document. (Discover Canada, page 10.)",
            topicGroupId = "q554"
        ),
        Question(
            id = 555,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Today, diversity enriches the lives of Canadians. Where is diversity reflected the most?",
            options = listOf("Countryside areas", "Cities", "Towns", "Mountains"),
            correctAnswerIndex = 1,
            explanation = "Canada's diversity is reflected most strongly in its cities, home to people from a wide range of backgrounds. (Discover Canada, page 25.)",
            topicGroupId = "q555"
        ),
        Question(
            id = 556,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What does the M.L.A. stand for?",
            options = listOf("Member of Legal Aid", "Member of the Legislative Assembly", "Member of Land Association", "Member of Land Aid"),
            correctAnswerIndex = 1,
            explanation = "M.L.A. stands for Member of the Legislative Assembly, a provincial representative (the title varies by province). (Discover Canada, page 29.)",
            topicGroupId = "q556"
        ),
        Question(
            id = 557,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who do provincial members of the legislative or national assemblies represent?",
            options = listOf("Federal and provincial governments", "Everyone who lives in the federal electoral district", "Everyone who lives in the provincial or territorial electoral district", "Everyone who lives in the municipal electoral district"),
            correctAnswerIndex = 2,
            explanation = "Provincial and territorial members represent everyone who lives in their provincial or territorial electoral district, regardless of how they voted.",
            topicGroupId = "q557"
        ),
        Question(
            id = 558,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is a ballot?",
            options = listOf("A form that tells you when and where to vote", "A dance", "A form for voting", "A form to count the number of votes"),
            correctAnswerIndex = 2,
            explanation = "A ballot is the form used to cast your vote. (Discover Canada, page 32.)",
            topicGroupId = "q558"
        ),
        Question(
            id = 559,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who elects the members to the House of Commons in Ottawa, and to the provincial and territorial legislatures?",
            options = listOf("The Government Employees", "The Government", "The Prime Minister", "The people"),
            correctAnswerIndex = 3,
            explanation = "Members of the House of Commons and of provincial and territorial legislatures are elected directly by the people. (Discover Canada, page 28.)",
            topicGroupId = "mps_elected_by_the_people"
        ),
        Question(
            id = 560,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Name two important documents that describe our rights and freedoms.",
            options = listOf("The Canadian Constitution and English common law", "The Civil Code of France and the Canadian Constitution", "The Canadian Charter of Rights and Freedoms and Magna Carta (the Great Charter of Freedoms)", "Laws passed by Parliament and English common law"),
            correctAnswerIndex = 2,
            explanation = "The Canadian Charter of Rights and Freedoms and the Magna Carta (the Great Charter of Freedoms) are the two documents Discover Canada names as describing the rights and freedoms of Canadians. (Discover Canada, page 8.)",
            topicGroupId = "q560"
        ),
        Question(
            id = 561,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the final step before a bill becomes law?",
            options = listOf("Approved by the King", "Approved by the Prime Minister", "Approved by the Governor General", "Approved by a judge"),
            correctAnswerIndex = 2,
            explanation = "The final step before a bill becomes law is Royal Assent, given by the Governor General on behalf of the King. (Discover Canada, page 29.)",
            topicGroupId = "q561"
        ),
        Question(
            id = 562,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "When you vote on election day, what do you do?",
            options = listOf("Go to the voting station, tell them who you are, and mark your X. Give the ballot back to the attendant.", "Go to the voting station, remove one ballot, mark your X, and deposit it in the ballot box.", "Go to the voting station, take your voter's card with proof of identity, highlight your choice on the ballot, and deposit it in the box.", "Go to the voting station, take your voter information card and ID, mark an X next to your chosen candidate, fold the ballot, and present it to the poll officials who will tear off the ballot number and give you the ballot to deposit in the box."),
            correctAnswerIndex = 3,
            explanation = "On election day, you bring your voter information card and ID to the polling station, mark an X next to your chosen candidate, fold the ballot, and hand it to a poll official, who tears off the ballot number before you deposit it in the box. (Discover Canada, page 32.)",
            topicGroupId = "q562"
        ),
        Question(
            id = 563,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following criteria give a Canadian the right to vote?",
            flashcardText = "What gives a Canadian the right to vote?",
            options = listOf("Owning a house", "Being on an official voters' list", "Having a driver's licence", "Being an immigrant"),
            correctAnswerIndex = 1,
            explanation = "Being on the official voters' list is one of the requirements to vote, along with being a Canadian citizen at least 18 years old. (Discover Canada, page 30.)",
            topicGroupId = "q563"
        ),
        Question(
            id = 564,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which legal documents protect the rights of Canadians with regards to the official languages?",
            options = listOf("British Charter of Rights and Freedoms", "Canadian Constitution and Official Languages Act", "Canadian Languages Act", "Official English Act"),
            correctAnswerIndex = 1,
            explanation = "The Canadian Constitution and the Official Languages Act together protect Canadians' rights regarding the official languages. (Discover Canada, page 39.)",
            topicGroupId = "q564"
        ),
        Question(
            id = 565,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the government responsible for all of Canada called?",
            options = listOf("The National Assembly", "The Legislature", "The Federal Government", "The Council"),
            correctAnswerIndex = 2,
            explanation = "The Federal Government is responsible for matters affecting the country as a whole. (Discover Canada, page 45.)",
            topicGroupId = "q565"
        ),
        Question(
            id = 566,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is the most popular spectator sport of Canada?",
            options = listOf("Soccer", "Canadian football", "Hockey", "Basketball"),
            correctAnswerIndex = 2,
            explanation = "Hockey is Canada's most popular spectator sport. (Discover Canada, page 39.)",
            topicGroupId = "hockey_most_popular_spectator_sport"
        ),
        Question(
            id = 567,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who are the Quebecois?",
            options = listOf("All the French-speaking people in Canada are called Quebecois.", "They form a nation within a united Canada.", "They are descendants of British settlers who live in Quebec.", "They are the Canadians who only speak French."),
            correctAnswerIndex = 1,
            explanation = "The House of Commons has recognized that the Quebecois form a nation within a united Canada. (Discover Canada, page 11.)",
            topicGroupId = "q567"
        ),
        Question(
            id = 568,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Approximately how many Canadians served in the First World War?",
            options = listOf("About 170,000", "About 10,000", "More than 60,000", "More than 600,000"),
            correctAnswerIndex = 3,
            explanation = "More than 600,000 Canadians served in the First World War. (Discover Canada, page 21.)",
            topicGroupId = "q568"
        ),
        Question(
            id = 569,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "When must federal elections be held?",
            options = listOf("Whenever the Prime Minister calls the election", "About every 4 years", "When the MPs want a new Prime Minister", "On the third Monday in October every four years following the most recent general election"),
            correctAnswerIndex = 3,
            explanation = "Federal law sets fixed election dates for the third Monday in October, every four years after the most recent general election, though the Prime Minister can still call an earlier one. (Discover Canada, page 30.)",
            topicGroupId = "federal_election_fixed_date_law"
        ),
        Question(
            id = 570,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which phrase embodied the vision for the Dominion of Canada?",
            options = listOf("\"The land of the strong and free\"", "\"Dominion from sea to sea and from the river to the ends of the earth\"", "\"Dominion from ocean to ocean\"", "\"O Canada, my home and native land\""),
            correctAnswerIndex = 1,
            explanation = "\"Dominion from sea to sea and from the river to the ends of the earth\" (evoking Psalm 72:8) embodied the vision for the Dominion of Canada. (Discover Canada, page 18.)",
            topicGroupId = "q570"
        ),
        Question(
            id = 571,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How are your rights and freedoms protected?",
            options = listOf("By the Charter of Rights and Freedoms", "By the King", "By citizenship", "None of the above"),
            correctAnswerIndex = 0,
            explanation = "Canadians' rights and freedoms are protected by the Canadian Charter of Rights and Freedoms. (Discover Canada, page 8.)",
            topicGroupId = "q571"
        ),
        Question(
            id = 572,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following statements is true regarding Canada's membership in international organizations?",
            flashcardText = "What is true regarding Canada's membership in international organizations?",
            options = listOf("Canada is a founding member of the United Nations but not of NATO.", "Canada is a founding member of NATO but not of the United Nations.", "Canada is a founding member of both the United Nations and NATO.", "Canada is not a founding member of either the United Nations or NATO."),
            correctAnswerIndex = 2,
            explanation = "Canada is a founding member of both the United Nations and NATO. (Discover Canada, page 24.)",
            topicGroupId = "q572"
        ),
        Question(
            id = 573,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who do Canadians vote for in a federal election?",
            options = listOf("A candidate whom they want to represent them in Parliament", "All candidates in their electoral district", "The best speaker running the election campaign", "Someone to become the Premier of the province"),
            correctAnswerIndex = 0,
            explanation = "In a federal election, Canadians vote for a candidate they want to represent them in Parliament. (Discover Canada, page 30.)",
            topicGroupId = "q573"
        ),
        Question(
            id = 574,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What year was Confederation?",
            options = listOf("1867", "1768", "1876", "1786"),
            correctAnswerIndex = 0,
            explanation = "Confederation occurred on July 1, 1867 - a well-established, frequently-cited fact. (Discover Canada, page 18.)",
            topicGroupId = "confederation_1867"
        ),
        Question(
            id = 575,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following is the responsibility of the federal government?",
            flashcardText = "What is the responsibility of the federal government?",
            options = listOf("Highways", "Currency", "Health", "Education"),
            correctAnswerIndex = 1,
            explanation = "Currency is a federal responsibility, unlike highways, health, and education, which fall to the provinces and territories. (Discover Canada, page 28.)",
            topicGroupId = "q575"
        ),
        Question(
            id = 576,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What did the Fathers of Confederation do to establish Canada?",
            options = listOf("They worked together to create a new country, the Dominion of Canada.", "They were explorers who organized an expedition to survey northern Canada.", "They formed a republic state in Canada.", "They were a group of politicians who attempted to join Canada to the United States."),
            correctAnswerIndex = 0,
            explanation = "The Fathers of Confederation worked together to create a new country, the Dominion of Canada. (Discover Canada, page 18.)",
            topicGroupId = "q576"
        ),
        Question(
            id = 577,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which province is the most easterly point in Canada?",
            options = listOf("Prince Edward Island", "New Brunswick", "Nova Scotia", "Newfoundland and Labrador"),
            correctAnswerIndex = 3,
            explanation = "Newfoundland and Labrador is Canada's most easterly province. (Discover Canada, page 46.)",
            topicGroupId = "q577"
        ),
        Question(
            id = 578,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Where is Canada's largest naval base located?",
            options = listOf("Vancouver", "Québec City", "Halifax", "Toronto"),
            correctAnswerIndex = 2,
            explanation = "Halifax is home to Canada's largest naval base. (Discover Canada, page 46.)",
            topicGroupId = "q578"
        ),
        Question(
            id = 579,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which one of the following is the most populated province in Canada?",
            options = listOf("Québec", "British Columbia", "Ontario", "Alberta"),
            correctAnswerIndex = 2,
            explanation = "Ontario has long been, and remains, Canada's most populous province.",
            topicGroupId = "ontario_most_populous_province"
        ),
        Question(
            id = 580,
            category = Category.WHO_WE_ARE,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Julia is a descendant of French colonists in the Maritime Province. What is she called?",
            options = listOf("Métis", "Acadian", "Inuit", "Indian"),
            correctAnswerIndex = 1,
            explanation = "A descendant of French colonists in the Maritime provinces is called an Acadian. (Discover Canada, page 11.)",
            topicGroupId = "acadian_definition"
        ),
        Question(
            id = 581,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Which of the following symbolizes close ties between Canada and the U.S.?",
            flashcardText = "What symbolizes close ties between Canada and the U.S.?",
            options = listOf("The Peace Arch in Blaine, Washington", "Statue of Liberty in New York", "International Peace Garden crossing between Canada and the United States", "White Pass in Yukon"),
            correctAnswerIndex = 0,
            explanation = "The Peace Arch in Blaine, Washington symbolizes the close ties and long peace between Canada and the United States. (Discover Canada, page 43.)",
            topicGroupId = "q581"
        ),
        Question(
            id = 582,
            category = Category.ECONOMY_GEOGRAPHY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "When did Canada's modern energy industry begin?",
            options = listOf("The economic boom of the 1890s and early 1900s", "After the War of 1812", "Since the discovery of oil in Alberta in 1947", "After the Second World War"),
            correctAnswerIndex = 2,
            explanation = "Canada's modern energy industry began with the discovery of oil in Alberta in 1947. (Discover Canada, page 24.)",
            topicGroupId = "q582"
        ),
        Question(
            id = 583,
            category = Category.RIGHTS_RESPONSIBILITIES,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "Who was the first female Member of Parliament?",
            options = listOf("Agnes Macphail", "Mary Ann Shadd Cary", "Laura Secord", "Alice Munro"),
            correctAnswerIndex = 0,
            explanation = "Agnes Macphail, elected in 1921, was Canada's first female Member of Parliament. (Discover Canada, page 21.)",
            topicGroupId = "q583"
        ),
        Question(
            id = 584,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "The Peace Tower was built in memory of:",
            options = listOf("The First World War", "The Second World War", "The Korean War", "The Battle of the Plains of Abraham"),
            correctAnswerIndex = 0,
            explanation = "The Peace Tower on Parliament Hill was built in memory of Canadians who died in the First World War. (Discover Canada, page 39.)",
            topicGroupId = "q584"
        ),
        Question(
            id = 585,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In 1939, Canada joined with its democratic allies to fight:",
            options = listOf("The USA", "Japan", "The Nazi", "Korea"),
            correctAnswerIndex = 2,
            explanation = "In 1939, Canada joined its democratic allies in declaring war on Nazi Germany, entering the Second World War. (Discover Canada, page 23.)",
            topicGroupId = "q585"
        ),
        Question(
            id = 586,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "How many Canadians have died in wars till now?",
            options = listOf("60,000", "110,000", "More than one million", "40,000"),
            correctAnswerIndex = 1,
            explanation = "More than 110,000 Canadians have died in the wars fought over the last century. (Discover Canada, page 22.)",
            topicGroupId = "q586"
        ),
        Question(
            id = 587,
            category = Category.HISTORY,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "In what year did Newfoundland and Labrador Join Canada?",
            options = listOf("1867", "1955", "1949", "1880"),
            correctAnswerIndex = 2,
            explanation = "Newfoundland and Labrador joined Confederation in 1949, the last province to do so. (Discover Canada, page 19.)",
            topicGroupId = "q587"
        ),
        Question(
            id = 588,
            category = Category.SYMBOLS,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What was made in 1927 after World War I?",
            options = listOf("The National War Memorial in Ottawa", "The Peace Arch between the United States and Canada", "The CN Tower in Toronto", "The Peace Tower"),
            correctAnswerIndex = 3,
            explanation = "The Peace Tower, completed in 1927, was built as a memorial after the First World War. (Discover Canada, page 39.)",
            topicGroupId = "q588"
        ),
        Question(
            id = 589,
            category = Category.GOVERNMENT,
            type = QuestionType.MULTIPLE_CHOICE,
            text = "What is a responsible government?",
            options = listOf("The government is responsible for the well-being of its people.", "The government must take responsibility for any act of war it decides to commit.", "A government that is against corruption.", "The government must resign if it loses a confidence vote in the assembly."),
            correctAnswerIndex = 3,
            explanation = "Consistent with the accountability-to-the-legislature principle underlying responsible government (see also Q48). (Discover Canada, page 18.)",
            topicGroupId = "confidence_vote_consequence_group"
        ),
    )
}
