package com.empresa.reembolsos;

import com.empresa.reembolsos.procesamiento.calculo.CalculadoraReembolso;
import com.empresa.reembolsos.procesamiento.notificacion.ObservadorCambioEstado;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

// Solo se analiza el codigo de produccion: las pruebas viven en los mismos paquetes y no son parte de la arquitectura
@AnalyzeClasses(packages = "com.empresa.reembolsos", importOptions = ImportOption.DoNotIncludeTests.class)
public class ReglasArquitectura {

    // Regla 1: procesamiento no depende de infraestructura, adaptadores ni JPA (las anotaciones de estereotipo si se permiten)
    @ArchTest
    static final ArchRule procesamientoSinPersistenciaNiWeb = noClasses()
            .that().resideInAPackage("..procesamiento..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "..infraestructura..", "..adaptadores..",
                    "org.springframework.data..", "jakarta.persistence..");

    // Regla 2: el adaptador REST solo accede al servicio, al dominio, a sus propios DTOs y a la capa web de Spring
    @ArchTest
    static final ArchRule controladorSoloUsaServicio = classes()
            .that().resideInAPackage("..adaptadores.rest..")
            .should().onlyAccessClassesThat()
            .resideInAnyPackage(
                    "..adaptadores.rest..", "..aplicacion..", "..dominio..",
                    "org.springframework.web..", "org.springframework.http..",
                    "org.springframework.validation..", "java..");

    // Regla 3: toda clase Calculadora* implementa la estrategia (SelectorCalculadora convive en el paquete y no es una)
    @ArchTest
    static final ArchRule calculadorasImplementanInterfaz = classes()
            .that().resideInAPackage("..procesamiento.calculo..")
            .and().areNotInterfaces()
            .and().haveSimpleNameStartingWith("Calculadora")
            .should().implement(CalculadoraReembolso.class);

    // Regla 4: todo lo concreto del paquete de notificacion es un observador
    @ArchTest
    static final ArchRule notificadoresImplementanInterfaz = classes()
            .that().resideInAPackage("..procesamiento.notificacion..")
            .and().areNotInterfaces()
            .should().implement(ObservadorCambioEstado.class);
}
