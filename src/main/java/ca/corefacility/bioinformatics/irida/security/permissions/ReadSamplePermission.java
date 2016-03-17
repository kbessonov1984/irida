package ca.corefacility.bioinformatics.irida.security.permissions;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.repositories.joins.project.ProjectSampleJoinRepository;
import ca.corefacility.bioinformatics.irida.repositories.sample.SampleRepository;

/**
 * Confirms that the authenticated user is allowed to read a sample.
 * 
 * 
 */
@Component
public class ReadSamplePermission extends BasePermission<Sample, Long> {

	private static final String PERMISSION_PROVIDED = "canReadSample";

	private final ProjectSampleJoinRepository psjRepository;
	private final ReadProjectPermission readProjectPermission;

	/**
	 * Construct an instance of {@link ReadSamplePermission}.
	 * 
	 * @param sampleRepository
	 *            the sample repository.
	 * @param userRepository
	 *            the user repository.
	 * @param pujRepository
	 *            the project user join repository.
	 * @param psjRepository
	 *            the project sample join repository.
	 */
	@Autowired
	public ReadSamplePermission(final SampleRepository sampleRepository,
			final ProjectSampleJoinRepository psjRepository, final ReadProjectPermission readProjectPermission) {
		super(Sample.class, Long.class, sampleRepository);
		this.psjRepository = psjRepository;
		this.readProjectPermission = readProjectPermission;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean customPermissionAllowed(Authentication authentication, Sample s) {

		// samples are always associated with a project. for a user to be
		// allowed to read a sample, the user must be part of the associated
		// project.

		List<Join<Project, Sample>> projectForSample = psjRepository.getProjectForSample(s);
		return projectForSample.stream().anyMatch(j -> readProjectPermission.isAllowed(authentication, j.getSubject()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPermissionProvided() {
		return PERMISSION_PROVIDED;
	}

}
