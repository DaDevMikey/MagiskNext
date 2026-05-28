import java.io.File
val a = File("testA")
a.mkdirs()
File(a, "index.html").writeText("hi")
val b = File("testB")
b.mkdirs()
a.copyRecursively(b, true)
println("index.html exists: " + File(b, "index.html").exists())
