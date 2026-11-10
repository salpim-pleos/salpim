package com.salpim.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class KoreanParticlesTest {
    @Test
    fun directionUsesEuroOnlyAfterNonRieulFinal() {
        assertEquals("블루핸즈 광희점으로", KoreanParticles.withDirection("블루핸즈 광희점"))
        assertEquals("서남주유소로", KoreanParticles.withDirection("서남주유소"))
        assertEquals("서울시청으로", KoreanParticles.withDirection("서울시청"))
        assertEquals("신당점 2호실로", KoreanParticles.withDirection("신당점 2호실"))
        assertEquals("서울로", KoreanParticles.withDirection("서울"))
    }

    @Test
    fun trailingBracketsAreSkipped() {
        assertEquals("재동주유소 (현대오일뱅크)로", KoreanParticles.withDirection("재동주유소 (현대오일뱅크)"))
        assertEquals("정비소 (광희점)으로", KoreanParticles.withDirection("정비소 (광희점)"))
    }

    @Test
    fun digitsAreReadAloud() {
        assertEquals("1번으로", KoreanParticles.withDirection("1번"))
        assertEquals("3으로", KoreanParticles.withDirection("3"))
        assertEquals("7로", KoreanParticles.withDirection("7"))
        assertEquals("2로", KoreanParticles.withDirection("2"))
    }

    @Test
    fun objectSubjectTopicFollowFinal() {
        assertEquals("엔진오일 부족을", KoreanParticles.withObject("엔진오일 부족"))
        assertEquals("블루핸즈를", KoreanParticles.withObject("블루핸즈"))
        assertEquals("주유소가", KoreanParticles.withSubject("주유소"))
        assertEquals("경고등이", KoreanParticles.withSubject("경고등"))
        assertEquals("에어백은", KoreanParticles.withTopic("에어백"))
        assertEquals("워셔액 부족은", KoreanParticles.withTopic("워셔액 부족"))
    }

    @Test
    fun emptyOrSymbolOnlyFallsBackToNoFinal() {
        assertEquals("로", KoreanParticles.withDirection(""))
        assertEquals("--를", KoreanParticles.withObject("--"))
    }
}
