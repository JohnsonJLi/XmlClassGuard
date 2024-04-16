import com.xml.guard.entensions.GuardExtension
import com.xml.guard.utils.allDependencyAndroidProjects
import com.xml.guard.utils.resDir
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
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
                        println("XmlInsertTask:> filter : ${it} >f  ${it.fileName}")
                        Files.isDirectory(it) && it.fileName.toString().startsWith("layout")
                    }
                    .forEach { layoutDir ->
                        println("XmlInsertTask:> ${layoutDir}")
                        layoutDir.toFile().listFiles { file -> file.isFile && file.extension == "xml" }?.forEach { layoutFile ->
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
            viewType == "LinearLayout" || viewType == "RelativeLayout" || viewType == "LinearLayout"
        ) {
            println("XmlInsertTask:> 添加新的元素")
            // 添加新的元素
            val newElement = document.createElement("View")
            newElement.setAttributeNS(
                "http://schemas.android.com/apk/res/android",
                "android:layout_width",
                "${Random.nextInt(0, 50)}dp"
            )
            newElement.setAttributeNS(
                "http://schemas.android.com/apk/res/android",
                "android:layout_height",
                "${Random.nextInt(0, 50)}dp"
            )
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
}