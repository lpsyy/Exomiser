/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.filters.FilterReport;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.filters.PassAllVariantEffectsFilter;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.monarchinitiative.exomiser.core.writers.OutputFormat.TSV_VARIANT;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@ExtendWith(MockitoExtension.class)
public class ResultsWriterUtilsTest {

    private static final String DEFAULT_OUTPUT_DIR = "results";
    private final Path vcfPath = Paths.get("wibble");
    
    @Mock
    private Gene passedGeneOne;
    @Mock
    private Gene passedGeneTwo;
    @Mock
    private Gene failedGene;
    
    public void registerMocks() {
        Mockito.when(passedGeneOne.passedFilters()).thenReturn(Boolean.TRUE);
        Mockito.when(passedGeneTwo.passedFilters()).thenReturn(Boolean.TRUE);
        Mockito.when(failedGene.passedFilters()).thenReturn(Boolean.FALSE);
    }

    private List<Gene> getGenes() {
        List<Gene> genes = List.of(passedGeneOne, passedGeneTwo, failedGene);
        registerMocks();
        return genes;
    }

    @Test
    void testNullVcfAndEmptyOutputPrefixUsesVcfFileName() {
        String result = ResultsWriterUtils.makeOutputFilename(null, "", OutputFormat.JSON, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(result, equalTo("results/exomiser.json"));
    }

    @Test
    void testEmptyOutputPrefixUsesVcfFileName() {
        String result = ResultsWriterUtils.makeOutputFilename(Paths.get("/data/vcf/sample1_genome.vcf.gz"), "", OutputFormat.TSV_VARIANT, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(result, equalTo("results/sample1_genome-exomiser.variants.tsv"));
    }

    @ParameterizedTest
    @CsvSource({
            "'', /data/vcf/sample1_genome.vcf.gz, " + DEFAULT_OUTPUT_DIR + "/sample1_genome-exomiser",
            "wibble, /data/vcf/sample1_genome.vcf.gz, wibble",
            "/, /data/vcf/sample1_genome.vcf.gz, /sample1_genome-exomiser",
            "/tmp, /data/vcf/sample1_genome.vcf.gz, /tmp",
            "/tmp/, /data/vcf/sample1_genome.vcf.gz, /tmp/sample1_genome-exomiser",
            "/tmp/., /data/vcf/sample1_genome.vcf.gz, /tmp/sample1_genome-exomiser",
            "/tmp/.., /data/vcf/sample1_genome.vcf.gz, /sample1_genome-exomiser",
            "/tmp, '', /tmp",
            "/tmp/, '', /tmp/exomiser",
            "/tmp/wibble/hoopy/frood, /data/vcf/sample1_genome.vcf.gz, /tmp/wibble/hoopy/frood",
            "/tmp/wibble/hoopy/frood/, /data/vcf/sample1_genome.vcf.gz, /tmp/wibble/hoopy/frood/sample1_genome-exomiser",
            "wibble/hoopy/frood, '', wibble/hoopy/frood",
            "wibble/hoopy/frood, /data/vcf/sample1_genome.vcf.gz, wibble/hoopy/frood",
            "wibble/hoopy/frood/, '', wibble/hoopy/frood/exomiser",
            "wibble/hoopy/frood/, /data/vcf/sample1_genome.vcf.gz, wibble/hoopy/frood/sample1_genome-exomiser",
    })
    void testDirectoryOutputPrefixUsesVcfFileName(String outputPrefix, String vcfPath, String expected) {
        String result = ResultsWriterUtils.makeOutputFilename(vcfPath.isEmpty() ? null : Path.of(vcfPath), outputPrefix, TSV_VARIANT, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(result, equalTo(expected + "." + TSV_VARIANT.getFileExtension()));
    }

    @Test
    public void testThatSpecifiedTsvFileExtensionIsPresent() {
        OutputFormat testedFormat = OutputFormat.TSV_GENE;
        OutputSettings settings = OutputSettings.builder().build();
        String result = ResultsWriterUtils.makeOutputFilename(vcfPath, settings.getOutputPrefix(), testedFormat, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(result, equalTo(DEFAULT_OUTPUT_DIR + "/wibble-exomiser.genes.tsv"));
    }

    @Test
    public void testThatSpecifiedVcfFileExtensionIsPresent() {
        OutputFormat testedFormat = OutputFormat.VCF;
        OutputSettings settings = OutputSettings.builder().build();
        String result = ResultsWriterUtils.makeOutputFilename(vcfPath, settings.getOutputPrefix(), testedFormat, ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        assertThat(result, equalTo(DEFAULT_OUTPUT_DIR + "/wibble-exomiser.vcf"));
    }

    @Test
    public void testThatSpecifiedOutputFormatDoesNotOverwriteGivenOutputPrefixFileExtension() {
        OutputFormat testedFormat = OutputFormat.VCF;
        String outputPrefix = "/user/jules/exomes/analysis/slartibartfast.xml";
        String result = ResultsWriterUtils.makeOutputFilename(vcfPath, outputPrefix, testedFormat, ModeOfInheritance.X_DOMINANT);
        assertThat(result, equalTo("/user/jules/exomes/analysis/slartibartfast.xml.vcf"));
    }
    
    @Test
    public void testDefaultOutputFormatIsNotDestroyedByIncorrectFileExtensionDetection() {
        OutputFormat testedFormat = OutputFormat.HTML;
        OutputSettings settings = OutputSettings.builder().build();
        String result = ResultsWriterUtils.makeOutputFilename(vcfPath, settings.getOutputPrefix(), testedFormat, ModeOfInheritance.ANY);
        assertThat(result, equalTo(DEFAULT_OUTPUT_DIR + "/wibble-exomiser.html"));
    }
    
    @Test
    public void testOutFileNameIsCombinationOfOutPrefixAndOutFormat() {
        OutputFormat outFormat = OutputFormat.TSV_GENE;
        String outFilePrefix = "user/subdir/geno/vcf/F0000009/F0000009";
        String actual = ResultsWriterUtils.makeOutputFilename(vcfPath, outFilePrefix, outFormat, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(actual, equalTo("user/subdir/geno/vcf/F0000009/F0000009.genes.tsv"));
    }

    @Test
    public void testOutputFileNameModeOfInheritanceExtensions() {
        OutputFormat testedFormat = OutputFormat.HTML;

        String any = ResultsWriterUtils.makeOutputFilename(vcfPath, "", testedFormat, ModeOfInheritance.ANY);
        assertThat(any, equalTo(DEFAULT_OUTPUT_DIR + "/wibble-exomiser.html"));

        String autoDom = ResultsWriterUtils.makeOutputFilename(vcfPath, "", testedFormat, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(autoDom, equalTo(DEFAULT_OUTPUT_DIR + "/wibble-exomiser.html"));

        String autoRec = ResultsWriterUtils.makeOutputFilename(vcfPath, "", testedFormat, ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        assertThat(autoRec, equalTo(DEFAULT_OUTPUT_DIR + "/wibble-exomiser.html"));

        String xDom = ResultsWriterUtils.makeOutputFilename(vcfPath, "", testedFormat, ModeOfInheritance.X_DOMINANT);
        assertThat(xDom, equalTo(DEFAULT_OUTPUT_DIR + "/wibble-exomiser.html"));

        String xRec = ResultsWriterUtils.makeOutputFilename(vcfPath, "", testedFormat, ModeOfInheritance.X_RECESSIVE);
        assertThat(xRec, equalTo(DEFAULT_OUTPUT_DIR + "/wibble-exomiser.html"));

        String mito = ResultsWriterUtils.makeOutputFilename(vcfPath, "", testedFormat, ModeOfInheritance.MITOCHONDRIAL);
        assertThat(mito, equalTo(DEFAULT_OUTPUT_DIR + "/wibble-exomiser.html"));
    }

    @Test
    public void canMakeEmptyVariantTypeCounterFromEmptyVariantEvaluations() {
        List<VariantEvaluation> variantEvaluations = Collections.emptyList();
        List<VariantEffectCount> variantTypeCounters = ResultsWriterUtils.makeVariantEffectCounters(Collections.emptyList(), variantEvaluations);
        assertThat(variantTypeCounters.isEmpty(), is(false));
        
        VariantEffectCount firstVariantTypeCount = variantTypeCounters.get(0);
        assertThat(firstVariantTypeCount.getVariantType(), notNullValue());
        assertThat(firstVariantTypeCount.getSampleVariantTypeCounts().isEmpty(), is(true));
    }
    
    @Test
    public void canMakeFilterReportsFromAnalysisReturnsEmptyListWhenNoFiltersAdded() {
        Sample sample = Sample.builder().build();
        Analysis analysis = Analysis.builder().build();
        AnalysisResults analysisResults = AnalysisResults.builder()
                .sample(sample)
                .analysis(analysis)
                .build();
        List<FilterReport> results = ResultsWriterUtils.makeFilterReports(analysis, analysisResults);

        assertThat(results.isEmpty(), is(true));
    }
    
    @Test
    public void canMakeFilterReportsFromAnalysis() {
        Sample sample = Sample.builder().build();
        Analysis analysis = Analysis.builder()
                .addStep(new PassAllVariantEffectsFilter())
                .build();

        AnalysisResults analysisResults = AnalysisResults.builder()
                .sample(sample)
                .analysis(analysis)
                .build();

        List<FilterReport> results = ResultsWriterUtils.makeFilterReports(analysis, analysisResults);

        for (FilterReport result : results) {
            assertThat(result, equalTo(new FilterReport(FilterType.VARIANT_EFFECT_FILTER, 0, 0, List.of("Removed variants with effects of type: []"))));
        }

        assertThat(results.isEmpty(), is(false));
    }

    @Test
    public void testMaxPassedGenesWhereMaxGenesIsZero() {
        assertThat(ResultsWriterUtils.getMaxPassedGenes(getGenes(), 0), equalTo(List.of(passedGeneOne, passedGeneTwo)));
    }

    @Test
    public void testMaxPassedGenesWhereMaxGenesIsOne() {
        assertThat(ResultsWriterUtils.getMaxPassedGenes(getGenes(), 1), equalTo(List.of(passedGeneOne)));
    }
    @Test
    public void testMaxPassedGenesWhereMaxGenesIsGreaterThanInputSize() {
        assertThat(ResultsWriterUtils.getMaxPassedGenes(getGenes(), 100), equalTo(List.of(passedGeneOne, passedGeneTwo)));
    }

    @ParameterizedTest
    @CsvSource({
            "'', " + DEFAULT_OUTPUT_DIR,
            "wibble, ''",
            "/, /",
            "/tmp, /",
            "/tmp/, /tmp",
            "/tmp/wibble/hoopy/frood, /tmp/wibble/hoopy",
            "/tmp/wibble/hoopy/frood/, /tmp/wibble/hoopy/frood",
            "wibble/hoopy/frood, wibble/hoopy",
            "wibble/hoopy/frood/, wibble/hoopy/frood",
            "~/wibble/hoopy/frood, ~/wibble/hoopy",
            "~/wibble/hoopy/frood/, ~/wibble/hoopy/frood",
            "/home/hhx640/exomiser-results/, /home/hhx640/exomiser-results",
            "/home/hhx640/exomiser-results, /home/hhx640",
    })
    void testResolveOutputDirEmptyPrefixReturnsDefaultOutput(String outputPrefix, String expectedDir) {
        Path outputDir = ResultsWriterUtils.resolveOutputDir(outputPrefix);
        assertThat(outputDir, equalTo(Path.of(expectedDir)));
    }
}
