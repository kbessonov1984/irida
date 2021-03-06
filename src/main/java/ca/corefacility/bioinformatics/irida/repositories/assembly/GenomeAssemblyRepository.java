package ca.corefacility.bioinformatics.irida.repositories.assembly;

import ca.corefacility.bioinformatics.irida.model.assembly.GenomeAssembly;
import ca.corefacility.bioinformatics.irida.model.assembly.UploadedAssembly;
import ca.corefacility.bioinformatics.irida.repositories.IridaJpaRepository;
import ca.corefacility.bioinformatics.irida.repositories.filesystem.FilesystemSupplementedRepository;

/**
 * A repository for storing and retrieving {@link GenomeAssembly} entities
 */
public interface GenomeAssemblyRepository
		extends FilesystemSupplementedRepository<UploadedAssembly>, IridaJpaRepository<GenomeAssembly, Long> {

	/**
	 * {@inheritDoc}
	 * <p>
	 * Save is overridden here instead of in FilesystemSupplementedRepository as it would throw a compilation error
	 */
	<S extends GenomeAssembly> S save(S entity);
}
