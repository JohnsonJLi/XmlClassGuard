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
import kotlin.io.path.pathString
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
                        val fileName = it.fileName.toString()
                        Files.isDirectory(it) && (fileName.startsWith("layout") || fileName.startsWith("drawable"))
                    }
                    .forEach { insertDir ->
                        println("XmlInsertTask:> insertDir : ${insertDir} >f  ${insertDir.fileName}")
                        insertDir.toFile().listFiles { file -> file.isFile && file.extension == "xml" }?.forEach { layoutFile ->
                            val tempFilePath = layoutFile.resolveSibling("${layoutFile.name}.temp").toPath()
                            println("XmlInsertTask:> ${tempFilePath}")
                            modifyXmlFile(layoutFile.toPath(), tempFilePath)
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun modifyXmlFile(input: Path, output: Path) {
        val isDrawable = input.pathString.contains("drawable")
        val isLayout = input.pathString.contains("layout")
        if (!isDrawable && !isLayout) return
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = documentBuilder.parse(input.toFile())
        val root = document.documentElement

        val viewType = root.tagName
        if (isDrawable) {
            if (viewType == "vector") {
                // 提取并解析原有 viewportWidth 和 viewportHeight 值
                val viewportWidthOriginal = root.getAttribute("android:viewportWidth").toDoubleOrNull() ?: 1440.0
//                val viewportHeightOriginal = root.getAttribute("android:viewportHeight").toDoubleOrNull() ?: 24.0

                val newX = viewportWidthOriginal + if (guard.resRandomFactor > 0) guard.resRandomFactor else Random.nextInt(10, 24).toInt()
//                val zoom = newW / viewportHeightOriginal
//                root.setAttribute("android:viewportWidth", newW.toInt().toString())
//                root.setAttribute("android:viewportHeight", (viewportHeightOriginal * zoom).toInt().toString())

                //    <path
                //        android:fillColor="#00000000"
                //        android:pathData="M 1440,0 m -0.1,0 a 0.1,0.1 0 1,0 0.2,0 a 0.1,0.1 0 1,0 -0.2,0" />
                val newElement = document.createElement("path")
                newElement.setAttributeNS(
                    "http://schemas.android.com/apk/res/android",
                    "android:fillColor",
                    "#00${generateRandomArgbColor()}"
                )
                newElement.setAttributeNS(
                    "http://schemas.android.com/apk/res/android",
                    "android:pathData",
                    "M ${newX},0 m -0.1,0 a 0.1,0.1 0 1,0 0.2,0 a 0.1,0.1 0 1,0 -0.2,0"
                )

                root.appendChild(newElement)
            } else {
                return
            }
        } else if (isLayout) {
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
                return
//            println("XmlInsertTask:> 在根标签中随机添加空格或回车")
//            // 在根标签中随机添加空格或回车
//            repeat(Random.nextInt(1, 50)) {
//                root.textContent += if (Random.nextBoolean()) " " else "\n"
//            }
            }
        }

        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.transform(DOMSource(document), StreamResult(output.toFile()))
        Files.move(output, input, StandardCopyOption.REPLACE_EXISTING)
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

    private fun generateRandomArgbColor(): String {
        val red = Random.nextInt(256)
        val green = Random.nextInt(256)
        val blue = Random.nextInt(256)

        // 合并四个分量为一个32位整数
        return "${red.toString(16).padStart(2, '0')}${green.toString(16).padStart(2, '0')}${blue.toString(16).padStart(2, '0')}"
    }
}