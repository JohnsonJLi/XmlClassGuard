package com.xml.guard.model

import com.xml.guard.entensions.GuardExtension
import com.xml.guard.utils.*
import org.gradle.api.Project
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.Writer


/**
 * User: ljx
 * Date: 2022/3/16
 * Time: 22:02
 */
class Mapping {

    companion object {
        internal const val DIR_MAPPING = "dir mapping:"
        internal const val CLASS_MAPPING = "class mapping:"
    }

    private val packageNameBlackList = mutableSetOf(
        "in", "is", "as", "if", "do", "by", "new", "try", "int", "for", "out", "var", "val", "fun",
        "byte", "void", "this", "else", "case", "open", "enum", "true", "false", "inner", "unit",
        "null", "char", "long", "super", "while", "break", "float", "final", "short", "const",
        "throw", "class", "catch", "return", "static", "import", "assert", "inline", "reified",
        "object", "sealed", "vararg", "suspend",
        "double", "native", "extends", "switch", "public", "package", "throws", "continue",
        "noinline", "lateinit", "internal", "companion",
        "default", "finally", "abstract", "private", "protected", "implements", "interface",
        "strictfp", "transient", "boolean", "volatile", "instanceof", "synchronized", "constructor"
    )

    internal val dirMapping = mutableMapOf<String, String>()
    internal val classMapping = mutableMapOf<String, String>()

    //类名索引
    internal var classIndex = -1L

    //包名索引
    internal var packageNameIndex = -1L

    //遍历文件夹下的所有直接子类，混淆文件名及移动目录
    fun obfuscateAllClass(project: Project, guardExtension: GuardExtension? = null): Map<String, String> {
        val classMapped = mutableMapOf<String, String>()
        val iterator = dirMapping.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val rawDir = entry.key
            println("rawDir : $rawDir = ${entry.value}")
            var locationProject = project.findLocationProject(rawDir)

            if (locationProject == null) {
                if (!guardExtension?.flavor.isNullOrEmpty()) {
                    locationProject = project.findLocationProject(rawDir, flavor = guardExtension!!.flavor!!)
                    if (locationProject != null) {
                        fileRename(locationProject, rawDir, guardExtension.flavor!!, classMapped, guardExtension)
                        continue
                    }
                }
                iterator.remove()
                continue
            }
            fileRename(locationProject, rawDir, "main", classMapped, guardExtension!!)

            locationProject = project.findLocationProject(rawDir, flavor = guardExtension.flavor!!)
            if (locationProject != null) {
                fileRename(locationProject, rawDir, guardExtension.flavor!!, classMapped, guardExtension)
            }
        }
        return classMapped
    }

    private fun fileRename(
        locationProject: Project,
        rawDir: String,
        flavor: String,
        classMapped: MutableMap<String, String>,
        guardExtension: GuardExtension?
    ) {
        val manifestPackage = locationProject.findPackage()
        //去除目录的直接子文件
        val dirPath = rawDir.replace(".", File.separator)


        val childFiles = locationProject.javaDirs(dirPath, flavor = flavor).flatMap {
            println("file : $it")
            val files = it.listFiles { f ->
                val filename = f.name
                f.isFile && (filename.endsWith(".java") || filename.endsWith(".kt"))
            }?.toList() ?: mutableListOf()
            files
        }
        if (childFiles.isEmpty()) {
            println("childFiles isEmpty : $rawDir")
            return
        }
        for (file in childFiles) {
            val rawClassPath = "${rawDir}.${file.name.removeSuffix()}"
            //已经混淆
            if (isObfuscated(rawClassPath)) {
                println("isObfuscated : $rawClassPath")
                continue
            }
            if (rawDir == manifestPackage) {
                file.insertImportXxxIfAbsent(manifestPackage)
            }
            //                val obfuscatePath = obfuscatePath(rawClassPath)

            val obfuscatePath = renamePath(rawClassPath, guardExtension)
            val relativePath = obfuscatePath.replace(".", File.separator) + file.name.getSuffix()
            val newFile = locationProject.javaDir(relativePath, file.absolutePath, flavor = flavor)
            if (!newFile.exists()) newFile.parentFile.mkdirs()
            newFile.writeText(file.readText())
            file.delete()
            classMapped[rawClassPath] = obfuscatePath
        }
    }

    fun isObfuscated(rawClassPath: String) = classMapping.containsValue(rawClassPath)

    fun isXmlInnerClass(classPath: String): Boolean {
        return classPath.contains("[a-zA-Z0-9_]+\\$[a-zA-Z0-9_]+".toRegex())
    }

    //混淆包名+类名，返回混淆后的包名+类名
    fun obfuscatePath(rawClassPath: String): String {
        var obfuscateClassPath = classMapping[rawClassPath]
        if (obfuscateClassPath == null) {
            val rawPackage = rawClassPath.getDirPath()
            val obfuscatePackage = obfuscatePackage(rawPackage)
            // 内部类，如：<service android:name=".TestBroadReceiver$NotifyJobService" />
            if (isXmlInnerClass(rawClassPath)) {
                obfuscateClassPath = "$obfuscatePackage.${generateObfuscateClassName()}"
                val arr = rawClassPath.split("$")
                classMapping[arr[0]] = obfuscateClassPath

                // 用于清单文件中替换
                obfuscateClassPath = "$obfuscateClassPath\$${arr[1]}"
                classMapping[rawClassPath] = obfuscateClassPath
            } else {
                obfuscateClassPath = "$obfuscatePackage.${generateObfuscateClassName()}"
                classMapping[rawClassPath] = obfuscateClassPath
            }
        }
        return obfuscateClassPath
    }

    fun renamePath(rawClassPath: String, guardExtension: GuardExtension?): String {
        var obfuscateClassPath = classMapping[rawClassPath]
        println("renamePath obfuscateClassPath : $obfuscateClassPath")
        if (obfuscateClassPath == null) {
            val rawPackage = rawClassPath.getDirPath()
            val rawName = rawClassPath.getSuffixName()
            var obfuscatePackage = dirMapping[rawPackage]
            if (obfuscatePackage == null) {
                dirMapping[rawPackage] = rawPackage
                obfuscatePackage = rawPackage
            }
            // 内部类，如：<service android:name=".TestBroadReceiver$NotifyJobService" />
            if (isXmlInnerClass(rawClassPath)) {
//                obfuscateClassPath = "$obfuscatePackage.${flavor.capitalize()}${nameHandling(rawName)}"
                obfuscateClassPath =
                    "$obfuscatePackage.${getSkitConfusionDictionary(getSkitConfusionDictionaryList(guardExtension))}${nameHandling(rawName)}"
                val arr = rawClassPath.split("$")
                classMapping[arr[0]] = obfuscateClassPath

                // 用于清单文件中替换
                obfuscateClassPath = "$obfuscateClassPath\$${arr[1]}"
                classMapping[rawClassPath] = obfuscateClassPath
            } else {
//                obfuscateClassPath = "$obfuscatePackage.${flavor.capitalize()}${nameHandling(rawName)}"
                obfuscateClassPath =
                    "$obfuscatePackage.${getSkitConfusionDictionary(getSkitConfusionDictionaryList(guardExtension))}${nameHandling(rawName)}"
                classMapping[rawClassPath] = obfuscateClassPath
            }
        }
        println("renamePath end : $obfuscateClassPath")
        return obfuscateClassPath
    }

    private fun nameHandling(rawName: String) =
        when {
            (rawName.endsWith("Activity")) -> {
                rawName.removeRange(rawName.length - 5, rawName.length)
            }

            (rawName.endsWith("Fragment")) -> {
                rawName.removeRange(rawName.length - 4, rawName.length)
            }

            (rawName.endsWith("Application")) -> {
                rawName.removeRange(rawName.length - 8, rawName.length)
            }

            else -> rawName
        }


    fun writeMappingToFile(mappingFile: File) {
        val writer: Writer = BufferedWriter(FileWriter(mappingFile, false))

        writer.write("$DIR_MAPPING\n")
        for ((key, value) in dirMapping) {
            writer.write(String.format("\t%s -> %s\n", key, value))
        }
        writer.write("\n")
        writer.flush()

        writer.write("$CLASS_MAPPING\n")
        for ((key, value) in classMapping.entries) {
            writer.write(String.format("\t%s -> %s\n", key, value))
        }
        writer.flush()

        writer.close()
    }

    //混淆包名，返回混淆后的包名
    private fun obfuscatePackage(rawPackage: String): String {
        var obfuscatePackage = dirMapping[rawPackage]
        if (obfuscatePackage == null) {
            obfuscatePackage = generateObfuscatePackageName()
            dirMapping[rawPackage] = obfuscatePackage
        }
        return obfuscatePackage
    }

    //生成混淆的包名
    private fun generateObfuscatePackageName(): String {
        var obfuscatePackage = (++packageNameIndex).toLetterStr()
        while (obfuscatePackage in packageNameBlackList) {
            //过滤黑名单
            obfuscatePackage = (++packageNameIndex).toLetterStr()
        }
        return obfuscatePackage
    }

    //生成混淆的类名
    private fun generateObfuscateClassName(): String {
        if (++classIndex == 17L) { //跳过字母 R
            classIndex++
        }
        return classIndex.toUpperLetterStr()
    }

    private fun hash(key: Any): Int {
        val h = key.hashCode()
        return h xor (h ushr 16)
    }

    private fun getSkitConfusionDictionary(list: List<String>): String {
        classIndex++
        val sb = StringBuffer(list[(classIndex % list.size).toInt()])
//        val numberRounds = (classIndex / list.size)
//        for (i in 1..numberRounds) {
//            sb.append(list.random())
//        }
        return sb.toString()
    }

    private fun getSkitConfusionDictionaryList(guardExtension: GuardExtension?): List<String> {
        return guardExtension?.obfuscatedDictionaries?.let {
            val list = mutableListOf<String>()
            it.forEachLine { line ->
                list.add(line.trim()) // 添加每一行到列表中，trim()用于移除可能的前导或尾随空白
            }
            list
        } ?: skitConfusionDictionaryDefList
    }

    private val skitConfusionDictionaryDefList =
        mutableListOf(
            "Scriptwriting",
            "CharacterDevelopment",
            "PlotStructure",
            "Storyboarding",
            "NarrativeTwist",
            "DialogueWriting",
            "Setting",
            "CharacterRelationships",
            "Synopsis",
            "ProductionMeeting",
            "WritersWorkshop",
            "IdeaBrainstorming",
            "ThemeExtraction",
            "GenreDefinition",
            "ComedyAspect",
            "Suspense",
            "Drama",
            "SketchComedy",
            "Sitcom",
            "Monologue",
            "EnsembleCast",
            "Pacing",
            "Climax",
            "Foreshadowing",
            "Flashback",
            "FlashForward",
            "PlotPoint",
            "IncitingIncident",
            "RisingAction",
            "Conflict",
            "Resolution",
            "CharacterArc",
            "Backstory",
            "Motivation",
            "Antagonist",
            "Protagonist",
            "SupportingCharacter",
            "PlotHole",
            "Continuity",
            "Editing",
            "Rehearsal",
            "Filming",
            "Cinematography",
            "SoundDesign",
            "MusicScore",
            "SpecialEffects",
            "CostumeDesign",
            "Makeup",
            "SetDesign",
            "LocationScouting",
            "PreProduction",
            "PrincipalPhotography",
            "PostProduction",
            "ScreenplayFormatting",
            "ActStructure",
            "SceneBreakdown",
            "BeatSheet",
            "Pitching",
            "Treatment",
            "Logline",
            "Tagline",
            "GenreBlending",
            "DarkComedy",
            "RomanticComedy",
            "Dramedy",
            "Parody",
            "Satire",
            "Farce",
            "Tragedy",
            "Melodrama",
            "Mystery",
            "Thriller",
            "Horror",
            "Fantasy",
            "ScienceFiction",
            "PeriodDrama",
            "AnthologySeries",
            "Webisode",
            "MiniSeries",
            "PilotEpisode",
            "SeriesBible",
            "CharacterSketch",
            "MoodBoard",
            "ConceptArt",
            "ScriptRevision",
            "TableRead",
            "FeedbackSession",
            "CreativeTeam",
            "Showrunner",
            "ExecutiveProducer",
            "LineProducer",
            "Director",
            "AssistantDirector",
            "CastingDirector",
            "TalentScout",
            "ActingCoach",
            "Cinematographer",
            "Gaffer",
            "Grip",
            "SoundMixer",
            "FoleyArtist",
            "Composer",
            "Choreographer",
            "StuntCoordinator",
            "VisualEffectsSupervisor",
            "ColorGrading",
            "SoundEditing",
            "Adr",
            "AutomatedDialogueReplacement",
            "Montage",
            "EstablishingShot",
            "CloseUp",
            "MediumShot",
            "LongShot",
            "TrackingShot",
            "DollyZoom",
            "CraneShot",
            "Steadicam",
            "HandheldCamera",
            "LowAngleShot",
            "HighAngleShot",
            "TwoShot",
            "OverTheShoulder",
            "PointOfViewShot",
            "FreezeFrame",
            "TimeLapse",
            "SlowMotion",
            "VoiceOver",
            "Narration",
            "ColdOpen",
            "Cliffhanger",
            "Callback",
            "EasterEgg",
            "CallbackHumor",
            "RunningGag",
            "TwistEnding",
            "CliffhangerResolution",
            "CharacterReveal",
            "PlotTwist",
            "Subplot",
            "ParallelNarrative",
            "NonLinearStorytelling",
            "EnsembleNarrative",
            "Metafiction",
            "BreakingTheFourthWall",
            "EpistolaryFormat",
            "FoundFootage",
            "Mockumentary",
            "Improvisation",
            "MethodActing",
            "PhysicalComedy",
            "Slapstick",
            "DeadpanHumor",
            "Irony",
            "Sarcasm",
            "Wit",
            "Wordplay",
            "Puns",
            "Juxtaposition",
            "Allusion",
            "Symbolism",
            "FoilCharacter",
            "ThemeSong",
            "OpeningCredits",
            "ClosingCredits",
            "Outtakes",
            "Bloopers",
            "DeletedScenes",
            "DirectorsCut",
            "ExtendedEdition",
            "TeaserTrailer",
            "OfficialTrailer",
            "MarketingCampaign",
            "SocialMediaBuzz",
            "Premiere",
            "AudienceFeedback",
            "Ratings",
            "NielsenRatings",
            "StreamingPlatform",
            "BroadcastNetwork",
            "CableTv",
            "Syndication",
            "LicensingDeal",
            "DistributionRights",
            "InternationalRelease",
            "FestivalCircuit",
            "AwardNomination",
            "Emmy",
            "GoldenGlobe",
            "Oscar",
            "Bafta",
            "CriticsChoiceAwards",
            "Fandom",
            "FanTheories",
            "Shipping",
            "SpoilerAlert",
            "Recap",
            "Analysis",
            "Review",
            "Critique",
            "CultFollowing",
            "SpinOff",
            "Crossover",
            "Revival",
            "Reboot",
            "Remake",
            "Adaptation",
            "BasedOnATrueStory",
            "GraphicNovelAdaptation",
            "BookToScreen",
            "AnthologyEpisode",
            "BottleEpisode",
            "MidSeasonFinale",
            "SeasonFinale",
            "SeriesFinale",
            "ChristmasSpecial",
            "HalloweenEpisode",
            "ThanksgivingEpisode",
            "HolidayTheme",
            "CulturalReference",
            "ProductPlacement",
            "BrandIntegration",
            "Merchandising",
            "TieInProducts",
            "SoundtrackRelease",
            "BehindTheScenesFeaturette",
            "MakingOfDocumentary",
            "DvdExtras",
            "BluRayExclusive",
            "DigitalDownload",
            "StreamingServiceExclusive",
            "LiveAudience",
            "LaughTrack",
            "MulticameraSetup",
            "SingleCameraSetup",
            "CinematicStyle",
            "RealisticPortrayal",
            "ExaggeratedForComedicEffect",
            "Surrealism",
            "Absurdity",
            "MagicalRealism",
            "PsychologicalDrama",
            "ExistentialThemes",
            "PhilosophicalMusings",
            "SocialCommentary",
            "PoliticalSatire",
            "EnvironmentalMessage",
            "HumanRightsAdvocacy",
            "LgbtqRepresentation",
            "DiversityAndInclusion",
            "CulturalDiversity",
            "GenderEquality",
            "AgeismInMedia",
            "DisabilityRepresentation",
            "MentalHealthAwareness",
            "HistoricalAccuracy",
            "PeriodAuthenticity",
            "FuturisticVision",
            "AlternateReality",
            "DystopianSociety",
            "UtopianFantasy",
            "TimeTravel",
            "SpaceExploration",
            "AlienEncounter",
            "SuperheroGenre",
            "AntiHero",
            "Superpower",
            "OriginStory",
            "SecretIdentity",
            "Supervillain",
            "EpicBattle",
            "SaveTheWorldPlot",
            "ComingOfAgeTale",
            "RiteOfPassage",
            "Bildungsroman",
            "LoveStory",
            "RomanceSubplot",
            "MeetCute",
            "LoveTriangle",
            "ForbiddenLove",
            "SecondChanceRomance",
            "FriendshipDynamics",
            "Bromance",
            "SisterhoodBond",
            "FamilyDrama",
            "SiblingRivalry",
            "ParentChildRelationship",
            "AdoptionStoryline",
            "BlendedFamily",
            "GenerationalConflict",
            "Aging",
            "MidlifeCrisis",
            "RetirementDreams",
            "Nostalgia",
            "ChildhoodMemories",
            "TeenAngst",
            "YoungAdulthoodChallenges",
            "AdulthoodMilestones",
            "MidLifeReflections",
            "SeniorWisdom",
            "LifeLessons",
            "RedemptionArc",
            "SelfDiscoveryJourney",
            "HerosJourney",
            "MentorFigure",
            "Sidekick",
            "EnsembleDynamics",
            "EnsembleChemistry",
            "EnsembleStorytelling",
            "EnsembleDialogue",
            "EnsembleHumor",
            "EnsembleAction",
            "EnsembleDrama",
            "EnsembleClimax",
            "EnsembleResolution",
            "EnsembleConclusion",
            "EnsembleLegacy",
            "EnsembleReunion",
            "EnsembleFarewell",
            "EnsembleTribute",
            "EnsembleCelebration",
            "EnsembleNostalgia",
            "EnsembleFuture",
            "EnsembleEvolution",
            "EnsembleGrowth",
            "EnsembleTransformation",
            "EnsembleResilience",
            "EnsembleSupport",
            "EnsembleBonding",
            "EnsembleCamaraderie",
            "EnsembleTeamwork",
            "EnsembleVictory",
            "EnsembleSacrifice",
            "EnsembleCourage",
            "EnsembleHeartbreak",
            "EnsembleHealing",
            "EnsembleLaughter",
            "EnsembleTears",
            "EnsembleMemories",
            "EnsembleAdventures",
            "EnsembleMysteries",
            "EnsembleSurprises",
            "EnsembleRevelations",
            "EnsembleConflicts",
            "EnsembleResolutions",
            "EnsembleMisunderstandings",
            "EnsembleApologies",
            "EnsembleForgiveness",
            "EnsembleUnity",
            "EnsembleDiversity",
            "EnsembleRepresentation",
            "EnsembleInspiration",
            "EnsembleMotivation",
            "EnsembleCreativity",
            "EnsembleImagination",
            "EnsembleInnovation",
            "EnsembleVision",
            "Mission",
            "EnsemblePurpose",
            "EnsembleDrive",
            "EnsemblePassion",
            "EnsembleDedication",
            "EnsemblePerseverance",
            "EnsembleBravery",
            "EnsembleWit",
            "EnsembleHumor",
            "EnsembleSatire",
            "EnsembleIrony",
            "EnsembleSarcasm",
            "EnsembleWordplay",
            "EnsemblePuns",
            "EnsembleWitBattles",
            "EnsembleOneLiners",
            "EnsemblePunchlines",
            "EnsembleComedicTiming",
            "EnsemblePhysicalComedy",
            "EnsembleSlapstick",
            "EnsembleDeadpan",
            "EnsembleComedicRelief",
            "EnsembleDramaticIrony",
            "EnsembleSituationalComedy",
            "EnsembleObservationalComedy",
            "EnsembleAbsurdComedy",
            "EnsembleBlackComedy",
            "EnsembleCringeComedy",
            "EnsembleParodyComedy",
            "EnsembleSketchComedy",
            "EnsembleStandUpComedy",
            "EnsembleImprovComedy",
            "EnsembleMusicalComedy",
            "EnsembleRomanticComedy",
            "EnsembleDarkComedy",
            "EnsembleAbsurdistHumor",
            "EnsembleIntelligentHumor",
            "EnsembleCulturalHumor",
            "EnsemblePoliticalHumor",
            "EnsembleSocialHumor",
            "EnsembleGenerationalHumor",
            "EnsembleFamilyHumor",
            "EnsembleFriendshipHumor",
            "EnsembleWorkplaceHumor",
            "EnsembleTravelHumor",
            "EnsembleHolidayHumor",
            "EnsembleFoodHumor",
            "EnsembleSportsHumor",
            "EnsembleTechHumor",
            "EnsembleSupernaturalHumor",
            "EnsembleHistoricalHumor",
            "EnsembleFuturisticHumor",
            "EnsembleFantasyHumor",
            "EnsembleSciFiHumor",
            "EnsembleAdventureHumor",
            "EnsembleDetectiveHumor",
            "EnsembleMysteryHumor",
            "EnsembleThrillerHumor",
            "EnsembleHorrorHumor",
            "EnsemblePsychologicalHumor",
            "EnsemblePhilosophicalHumor",
            "EnsembleExistentialHumor",
            "EnsembleSelfDeprecatingHumor",
            "EnsembleSatiricalCommentary",
            "EnsembleCriticalHumor",
            "EnsembleParodyOfLife",
            "EnsembleEverydayHumor",
            "EnsembleSituationalIrony",
            "EnsembleVerbalIrony",
            "DramaticTension",
            "EnsembleSuspensefulMoments",
            "EnsembleThrillingSequences",
            "EnsembleHeartPumpingAction",
            "EnsembleHighStakesDrama",
            "EnsembleEmotionalRollercoasters",
            "EnsembleCliffhangingScenes",
            "EnsemblePlotTwistsAndTurns",
            "EnsembleUnexpectedSurprises",
            "EnsembleShockingRevelations",
            "EnsembleIntenseConfrontations",
            "EnsembleClimacticShowdowns",
            "EnsembleDramaticResolutions",
            "EnsembleEmotionalPayoffs",
            "EnsembleCatharticMoments",
            "EnsembleTearjerkerScenes",
            "EnsembleBittersweetEndings",
            "EnsembleUpliftingConclusions",
            "EnsembleHopefulMessages",
            "EnsembleInspiringThemes",
            "EnsembleMotivationalSpeeches",
            "EnsembleEmpoweringNarratives",
            "ThoughtProvokingContent",
            "EnsembleEducationalElements",
            "EnsembleCulturalExplorations",
            "EnsembleHistoricalInsights",
            "EnsembleScientificExplanations",
            "EnsemblePhilosophicalDebates",
            "EthicalDilemmas",
            "MoralQuandaries",
            "SocialCommentaries",
            "PoliticalDiscussions",
            "EnvironmentalAwareness",
            "HumanitarianCauses",
            "DiversityAppreciation",
            "InclusivityPromotion",
            "RepresentationMatters",
            "EqualityAdvocacy",
            "JusticeThemes",
            "FreedomStruggles",
            "IdentityExplorations",
            "SelfExpressionEncouragement",
            "ArtisticInspirations",
            "CreativeProcesses",
            "WritersBlockSolutions",
            "DirectorialVision",
            "CollaborativeCreativity",
            "ProductionChallenges",
            "BudgetConstraints",
            "TimeManagement",
            "CastingDecisions",
            "LocationScouting",
            "PropSelection",
            "CostumeConsiderations",
            "MakeupAndHairDesign",
            "LightingTechniques",
            "SoundscapesCreation",
            "SpecialEffectsWizardry",
            "PostProductionMagic",
            "MarketingStrategies",
            "AudienceEngagement",
            "FanInteractions",
            "SocialMediaPresence",
            "DigitalMarketing",
            "PromotionalEvents",
            "PressTours",
            "AwardCampaigns"
        )

}