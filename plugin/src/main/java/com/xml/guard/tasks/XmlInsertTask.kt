import com.xml.guard.entensions.GuardExtension
import com.xml.guard.utils.allDependencyAndroidProjects
import com.xml.guard.utils.resDir
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.random.Random

open class XmlInsertTask @Inject constructor(
    guardExtension: GuardExtension
) : DefaultTask() {
    private val guard: GuardExtension = guardExtension

    private val views = arrayListOf("View", "TextView", "FrameLayout", "LinearLayout", "RelativeLayout")
    private val directions = arrayListOf("Start", "Bottom", "Top", "End", "Left", "Right", "")

    @TaskAction
    fun execute() {
        val androidProjects = allDependencyAndroidProjects()
        androidProjects.forEach { itP ->
            xmlInsert(itP.resDir().toPath())
            guard.flavor?.let {
                xmlInsert(itP.resDir(it).toPath())
            }
        }
    }

    private fun xmlInsert(projectDir: Path?) {
        if (!Files.notExists(projectDir) && Files.isDirectory(projectDir)) {
            println("XmlInsertTask:> ${projectDir}")
            try {
                Files.walk(projectDir)
                    .filter {
                        Files.isDirectory(it) && it.fileName.toString().startsWith("layout")
                    }
                    .forEach { insertDir ->
                        println("XmlInsertTask:> insertDir : ${insertDir} >f  ${insertDir.fileName}")
                        insertDir.toFile().listFiles { file -> file.isFile && file.extension == "xml" }?.forEach { layoutFile ->
                            val tempFilePath = layoutFile.resolveSibling("${layoutFile.name}.temp").toPath()
                            println("XmlInsertTask:> ${tempFilePath}")
                            modifyXmlFile(layoutFile.toPath(), tempFilePath)
                            Files.move(tempFilePath, layoutFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun modifyXmlFile(input: Path, output: Path) {
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = documentBuilder.parse(input.toFile())
        val root = document.documentElement

        val viewType = root.tagName
        if (viewType == "ViewGroup" || viewType == "androidx.constraintlayout.widget.ConstraintLayout" ||
            viewType == "LinearLayout" || viewType == "RelativeLayout" || viewType == "FrameLayout"
        ) {
            val randomView = views.random()
            println("XmlInsertTask:> 添加新的元素:$randomView")
            // 添加新的元素
            val newElement = document.createElement(randomView)
            newElement.setAttributeNS(
                "http://schemas.android.com/apk/res/android",
                "android:layout_width",
                "${getRandomWH()}dp"
            )
            newElement.setAttributeNS(
                "http://schemas.android.com/apk/res/android",
                "android:layout_height",
                "${getRandomWH()}dp"
            )

            randomAttributes(newElement)

            newElement.setAttributeNS(
                "http://schemas.android.com/apk/res/android",
                "android:visibility",
                "gone"
            )
            root.appendChild(newElement)
        } else {
//            println("XmlInsertTask:> 在根标签中随机添加空格或回车")
//            // 在根标签中随机添加空格或回车
//            repeat(Random.nextInt(1, 50)) {
//                root.textContent += if (Random.nextBoolean()) " " else "\n"
//            }
        }

        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.transform(DOMSource(document), StreamResult(output.toFile()))
    }

    private fun randomAttributes(newElement: Element) {
        when (Random.nextInt(0, 6)) {
            1 -> {
                newElement.setAttributeNS(
                    "http://schemas.android.com/apk/res/android",
                    "android:padding${directions.random()}",
                    "${getRandomWH()}dp"
                )
            }
            2 -> {
                newElement.setAttributeNS(
                    "http://schemas.android.com/apk/res/android",
                    "android:layout_margin",
                    "${Random.nextInt(0, 50)}dp"
                )
            }
            0 -> {
                randomAttributes(newElement)
            }
            else -> {
                newElement.setAttributeNS(
                    "http://schemas.android.com/apk/res/android",
                    "android:layout_margin${directions.random()}",
                    "${Random.nextInt(0, 50)}dp"
                )
            }
        }
    }

    private fun getRandomWH() = if (guard.resRandomFactor > 0) Random.nextInt(0, 5) * guard.resRandomFactor else Random.nextInt(0, 50)
}