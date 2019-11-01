package nsu.fit.javaperf;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class Agent {
    static int counter = 0;

    public static void premain(String args, Instrumentation instrum) {

        try{
            instrum.addTransformer(new ClassFileTransformer() {
                @Override
                public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                    if(className.equalsIgnoreCase("nsu.fit.javaperf.TransactionProcessor")){
                        try {
                            ClassPool cp = ClassPool.getDefault();
                            cp.insertClassPath(new ByteArrayClassPath(className, classfileBuffer));
                            CtClass ctClass = cp.get(className);
                            addTiming(ctClass);
                            classfileBuffer = ctClass.toBytecode();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ++counter;

                    System.out.println("load class: " + className.replaceAll("/", "."));
                    return classfileBuffer;
            }

            });

        } catch(Exception e){
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new CountingThread());
    }

    private static void addTiming(CtClass ctClass) throws NotFoundException, CannotCompileException {
        String actualName = "processTransaction";
        String customActualName = actualName + "$impl";

        CtMethod oldMethod = ctClass.getDeclaredMethod(actualName);
        oldMethod.setName(customActualName);
        CtMethod newMethod = CtNewMethod.copy(oldMethod, actualName, ctClass, null);

        StringBuilder body = new StringBuilder();
        body.append("{\nlong start = System.currentTimeMillis();\n");
        body.append("System.out.println(\"Call to method ")
                .append(actualName)
                .append(" took \" +\n (System.currentTimeMillis() - start) + ")
                .append("\" ms.\");\n")
                .append("}");

        newMethod.setBody(body.toString());
        ctClass.addMethod(newMethod);

    }
}
