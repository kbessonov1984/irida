package ca.corefacility.bioinformatics.irida.service.impl.unit;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ca.corefacility.bioinformatics.irida.exceptions.ExecutionManagerException;
import ca.corefacility.bioinformatics.irida.model.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.enums.AnalysisState;
import ca.corefacility.bioinformatics.irida.model.project.ReferenceFile;
import ca.corefacility.bioinformatics.irida.model.workflow.WorkflowState;
import ca.corefacility.bioinformatics.irida.model.workflow.WorkflowStatus;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.AnalysisPhylogenomicsPipeline;
import ca.corefacility.bioinformatics.irida.model.workflow.galaxy.phylogenomics.RemoteWorkflowPhylogenomics;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.galaxy.phylogenomics.AnalysisSubmissionPhylogenomics;
import ca.corefacility.bioinformatics.irida.repositories.analysis.submission.AnalysisSubmissionRepository;
import ca.corefacility.bioinformatics.irida.service.AnalysisExecutionScheduledTask;
import ca.corefacility.bioinformatics.irida.service.AnalysisSubmissionService;
import ca.corefacility.bioinformatics.irida.service.analysis.execution.galaxy.phylogenomics.impl.AnalysisExecutionServicePhylogenomics;
import ca.corefacility.bioinformatics.irida.service.impl.AnalysisExecutionScheduledTaskImpl;

/**
 * Tests out scheduling analysis tasks.
 * 
 * @author Aaron Petkau <aaron.petkau@phac-aspc.gc.ca>
 *
 */
public class AnalysisExecutionScheduledTaskImplTest {

	@Mock
	private AnalysisSubmissionService analysisSubmissionService;
	@Mock
	private AnalysisSubmissionRepository analysisSubmissionRepository;
	@Mock
	private AnalysisExecutionServicePhylogenomics analysisExecutionServicePhylogenomics;

	@Mock
	private RemoteWorkflowPhylogenomics remoteWorkflow;

	@Mock
	private Set<SequenceFile> sequenceFiles;

	@Mock
	private ReferenceFile referenceFile;

	@Mock
	private AnalysisPhylogenomicsPipeline analysis;

	private static final String ANALYSIS_ID = "1";
	private static final Long INTERNAL_ID = 1L;
	private AnalysisSubmissionPhylogenomics analysisSubmission;

	private AnalysisExecutionScheduledTask analysisExecutionScheduledTask;
	
	private UUID workflowId = UUID.randomUUID();

	/**
	 * Sets up variables for tests.
	 */
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		analysisExecutionScheduledTask = new AnalysisExecutionScheduledTaskImpl(
				analysisSubmissionService, analysisSubmissionRepository,
				analysisExecutionServicePhylogenomics);

		analysisSubmission = new AnalysisSubmissionPhylogenomics("my analysis", sequenceFiles,
				referenceFile, remoteWorkflow, workflowId);
		analysisSubmission.setId(INTERNAL_ID);
		analysisSubmission.setRemoteAnalysisId(ANALYSIS_ID);

		when(
				analysisSubmissionRepository.getByType(INTERNAL_ID,
						AnalysisSubmissionPhylogenomics.class)).thenReturn(
				analysisSubmission);
	}

	/**
	 * Tests successfully executing submitted analyses.
	 * 
	 * @throws ExecutionManagerException
	 */
	@Test
	public void testExecuteAnalysesSuccess() throws ExecutionManagerException {
		analysisSubmission.setAnalysisState(AnalysisState.NEW);

		when(
				analysisSubmissionRepository
						.findByAnalysisState(AnalysisState.NEW)).thenReturn(
				Arrays.asList(analysisSubmission));

		when(
				analysisExecutionServicePhylogenomics
						.prepareSubmission(analysisSubmission)).thenReturn(
				analysisSubmission);

		analysisExecutionScheduledTask.executeAnalyses();

		verify(analysisSubmissionRepository).findByAnalysisState(
				AnalysisState.NEW);
		verify(analysisSubmissionService).setStateForAnalysisSubmission(
				INTERNAL_ID, AnalysisState.PREPARING);
		verify(analysisExecutionServicePhylogenomics).prepareSubmission(
				analysisSubmission);
		verify(analysisSubmissionService).setStateForAnalysisSubmission(
				INTERNAL_ID, AnalysisState.SUBMITTING);
		verify(analysisExecutionServicePhylogenomics).executeAnalysis(
				analysisSubmission);
		verify(analysisSubmissionService).setStateForAnalysisSubmission(
				INTERNAL_ID, AnalysisState.RUNNING);
	}

	/**
	 * Tests no analyses to submit.
	 * 
	 * @throws ExecutionManagerException
	 */
	@Test
	public void testExecuteAnalysesNoAnalyses()
			throws ExecutionManagerException {

		when(
				analysisSubmissionRepository
						.findByAnalysisState(AnalysisState.NEW)).thenReturn(
				new ArrayList<AnalysisSubmission>());

		analysisExecutionScheduledTask.executeAnalyses();

		verify(analysisSubmissionRepository).findByAnalysisState(
				AnalysisState.NEW);
		verify(analysisExecutionServicePhylogenomics, never())
				.prepareSubmission(analysisSubmission);
	}

	/**
	 * Tests preparing an analysis and receiving an error exception. state.
	 * 
	 * @throws ExecutionManagerException
	 */
	@Test
	public void testExecuteAnalysesPrepareError()
			throws ExecutionManagerException {
		analysisSubmission.setAnalysisState(AnalysisState.NEW);

		when(
				analysisSubmissionRepository
						.findByAnalysisState(AnalysisState.NEW)).thenReturn(
				Arrays.asList(analysisSubmission));

		when(
				analysisExecutionServicePhylogenomics
						.prepareSubmission(analysisSubmission)).thenThrow(
				new ExecutionManagerException());

		analysisExecutionScheduledTask.executeAnalyses();

		verify(analysisSubmissionRepository).findByAnalysisState(
				AnalysisState.NEW);
		verify(analysisSubmissionService).setStateForAnalysisSubmission(
				INTERNAL_ID, AnalysisState.PREPARING);
		verify(analysisExecutionServicePhylogenomics).prepareSubmission(
				analysisSubmission);
		verify(analysisExecutionServicePhylogenomics, never()).executeAnalysis(
				analysisSubmission);
		verify(analysisSubmissionService).setStateForAnalysisSubmission(
				INTERNAL_ID, AnalysisState.ERROR);
	}

	/**
	 * Tests preparing an analysis and receiving an some random error exception.
	 * 
	 * @throws ExecutionManagerException
	 */
	@Test
	public void testExecuteAnalysesPrepareOtherException()
			throws ExecutionManagerException {
		analysisSubmission.setAnalysisState(AnalysisState.NEW);

		when(
				analysisSubmissionRepository
						.findByAnalysisState(AnalysisState.NEW)).thenReturn(
				Arrays.asList(analysisSubmission));

		when(
				analysisExecutionServicePhylogenomics
						.prepareSubmission(analysisSubmission)).thenThrow(
				new RuntimeException());

		analysisExecutionScheduledTask.executeAnalyses();

		verify(analysisSubmissionRepository).findByAnalysisState(
				AnalysisState.NEW);
		verify(analysisSubmissionService).setStateForAnalysisSubmission(
				INTERNAL_ID, AnalysisState.PREPARING);
		verify(analysisExecutionServicePhylogenomics).prepareSubmission(
				analysisSubmission);
		verify(analysisExecutionServicePhylogenomics, never()).executeAnalysis(
				analysisSubmission);
		verify(analysisSubmissionService).setStateForAnalysisSubmission(
				INTERNAL_ID, AnalysisState.ERROR);
	}

	/**
	 * Tests executing submitted analyses and moving it to an error state.
	 * 
	 * @throws ExecutionManagerException
	 */
	@Test
	public void testExecuteAnalysesExecuteError()
			throws ExecutionManagerException {
		analysisSubmission.setAnalysisState(AnalysisState.NEW);

		when(
				analysisSubmissionRepository
						.findByAnalysisState(AnalysisState.NEW)).thenReturn(
				Arrays.asList(analysisSubmission));

		when(
				analysisExecutionServicePhylogenomics
						.prepareSubmission(analysisSubmission)).thenReturn(
				analysisSubmission);

		when(
				analysisExecutionServicePhylogenomics
						.executeAnalysis(analysisSubmission)).thenThrow(
				new ExecutionManagerException());

		analysisExecutionScheduledTask.executeAnalyses();

		verify(analysisSubmissionRepository).findByAnalysisState(
				AnalysisState.NEW);
		verify(analysisSubmissionService).setStateForAnalysisSubmission(
				INTERNAL_ID, AnalysisState.PREPARING);
		verify(analysisExecutionServicePhylogenomics).prepareSubmission(
				analysisSubmission);
		verify(analysisSubmissionService).setStateForAnalysisSubmission(
				INTERNAL_ID, AnalysisState.SUBMITTING);
		verify(analysisExecutionServicePhylogenomics).executeAnalysis(
				analysisSubmission);
		verify(analysisSubmissionService).setStateForAnalysisSubmission(
				INTERNAL_ID, AnalysisState.ERROR);
	}

	/**
	 * Tests successfully transfering results for a submitted analysis.
	 * 
	 * @throws ExecutionManagerException
	 * @throws IOException
	 */
	@Test
	public void testTransferAnalysesResultsSuccess()
			throws ExecutionManagerException, IOException {
		analysisSubmission.setAnalysisState(AnalysisState.RUNNING);

		when(
				analysisSubmissionRepository
						.findByAnalysisState(AnalysisState.RUNNING))
				.thenReturn(Arrays.asList(analysisSubmission));
		when(
				analysisExecutionServicePhylogenomics
						.transferAnalysisResults(analysisSubmission))
				.thenReturn(analysis);
		when(
				analysisExecutionServicePhylogenomics
						.getWorkflowStatus(analysisSubmission)).thenReturn(
				new WorkflowStatus(WorkflowState.OK, 100.0f));

		analysisExecutionScheduledTask.transferAnalysesResults();

		verify(analysisSubmissionRepository).findByAnalysisState(
				AnalysisState.RUNNING);
		verify(analysisExecutionServicePhylogenomics).getWorkflowStatus(
				analysisSubmission);
		verify(analysisExecutionServicePhylogenomics).transferAnalysisResults(
				analysisSubmission);
		verify(analysisSubmissionService).setStateForAnalysisSubmission(
				INTERNAL_ID, AnalysisState.COMPLETED);
	}

	/**
	 * Tests no analysis results to check if they can be transferred.
	 * 
	 * @throws ExecutionManagerException
	 * @throws IOException
	 */
	@Test
	public void testTransferAnalysesResultsNoAnalyses()
			throws ExecutionManagerException, IOException {
		when(
				analysisSubmissionRepository
						.findByAnalysisState(AnalysisState.RUNNING))
				.thenReturn(new ArrayList<AnalysisSubmission>());

		analysisExecutionScheduledTask.transferAnalysesResults();

		verify(analysisSubmissionRepository).findByAnalysisState(
				AnalysisState.RUNNING);
	}

	/**
	 * Tests skipping over transfering results for a submitted analysis that's
	 * still running.
	 * 
	 * @throws ExecutionManagerException
	 * @throws IOException
	 */
	@Test
	public void testTransferAnalysesResultsSuccessNotComplete()
			throws ExecutionManagerException, IOException {
		analysisSubmission.setAnalysisState(AnalysisState.RUNNING);

		when(
				analysisSubmissionRepository
						.findByAnalysisState(AnalysisState.RUNNING))
				.thenReturn(Arrays.asList(analysisSubmission));
		when(
				analysisExecutionServicePhylogenomics
						.transferAnalysisResults(analysisSubmission))
				.thenReturn(analysis);
		when(
				analysisExecutionServicePhylogenomics
						.getWorkflowStatus(analysisSubmission)).thenReturn(
				new WorkflowStatus(WorkflowState.RUNNING, 50.0f));

		analysisExecutionScheduledTask.transferAnalysesResults();

		verify(analysisSubmissionRepository).findByAnalysisState(
				AnalysisState.RUNNING);
		verify(analysisExecutionServicePhylogenomics).getWorkflowStatus(
				analysisSubmission);
		verify(analysisExecutionServicePhylogenomics, never())
				.transferAnalysisResults(analysisSubmission);
	}

	/**
	 * Tests placing analysis results in an error state due to an error state in
	 * Galaxy.
	 * 
	 * @throws ExecutionManagerException
	 * @throws IOException
	 */
	@Test
	public void testTransferAnalysesGalaxyError()
			throws ExecutionManagerException, IOException {
		analysisSubmission.setAnalysisState(AnalysisState.RUNNING);

		when(
				analysisSubmissionRepository
						.findByAnalysisState(AnalysisState.RUNNING))
				.thenReturn(Arrays.asList(analysisSubmission));
		when(
				analysisExecutionServicePhylogenomics
						.transferAnalysisResults(analysisSubmission))
				.thenReturn(analysis);
		when(
				analysisExecutionServicePhylogenomics
						.getWorkflowStatus(analysisSubmission)).thenReturn(
				new WorkflowStatus(WorkflowState.ERROR, 50.0f));

		analysisExecutionScheduledTask.transferAnalysesResults();

		verify(analysisSubmissionRepository).findByAnalysisState(
				AnalysisState.RUNNING);
		verify(analysisExecutionServicePhylogenomics).getWorkflowStatus(
				analysisSubmission);
		verify(analysisSubmissionService).setStateForAnalysisSubmission(
				INTERNAL_ID, AnalysisState.ERROR);
		verify(analysisExecutionServicePhylogenomics, never())
				.transferAnalysisResults(analysisSubmission);
	}

	/**
	 * Tests placing analysis results in an error state due to an error
	 * transfering results from Galaxy.
	 * 
	 * @throws ExecutionManagerException
	 * @throws IOException
	 */
	@Test
	public void testTransferAnalysesTransferError()
			throws ExecutionManagerException, IOException {
		analysisSubmission.setAnalysisState(AnalysisState.RUNNING);

		when(
				analysisSubmissionRepository
						.findByAnalysisState(AnalysisState.RUNNING))
				.thenReturn(Arrays.asList(analysisSubmission));
		when(
				analysisExecutionServicePhylogenomics
						.transferAnalysisResults(analysisSubmission))
				.thenThrow(new ExecutionManagerException());
		when(
				analysisExecutionServicePhylogenomics
						.getWorkflowStatus(analysisSubmission)).thenReturn(
				new WorkflowStatus(WorkflowState.OK, 100.0f));

		analysisExecutionScheduledTask.transferAnalysesResults();

		verify(analysisSubmissionRepository).findByAnalysisState(
				AnalysisState.RUNNING);
		verify(analysisExecutionServicePhylogenomics).getWorkflowStatus(
				analysisSubmission);
		verify(analysisSubmissionService).setStateForAnalysisSubmission(
				INTERNAL_ID, AnalysisState.FINISHED_RUNNING);
		verify(analysisExecutionServicePhylogenomics).transferAnalysisResults(
				analysisSubmission);
		verify(analysisSubmissionService).setStateForAnalysisSubmission(
				INTERNAL_ID, AnalysisState.ERROR);
	}

	/**
	 * Tests placing analysis results in an error state due to an a generic
	 * exception.
	 * 
	 * @throws ExecutionManagerException
	 * @throws IOException
	 */
	@Test
	public void testTransferAnalysesOtherException()
			throws ExecutionManagerException, IOException {
		analysisSubmission.setAnalysisState(AnalysisState.RUNNING);

		when(
				analysisSubmissionRepository
						.findByAnalysisState(AnalysisState.RUNNING))
				.thenReturn(Arrays.asList(analysisSubmission));
		when(
				analysisExecutionServicePhylogenomics
						.transferAnalysisResults(analysisSubmission))
				.thenThrow(new RuntimeException());

		analysisExecutionScheduledTask.transferAnalysesResults();

		verify(analysisSubmissionRepository).findByAnalysisState(
				AnalysisState.RUNNING);
		verify(analysisExecutionServicePhylogenomics, never())
				.transferAnalysisResults(analysisSubmission);
		verify(analysisSubmissionService).setStateForAnalysisSubmission(
				INTERNAL_ID, AnalysisState.ERROR);
	}
}