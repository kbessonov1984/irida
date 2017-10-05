package ca.corefacility.bioinformatics.irida.ria.unit.web.analysis;

import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import com.google.common.collect.ImmutableList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;

public class AnalysesDataFactory {
	protected static Page<AnalysisSubmission> getPagedAnalysisSubmissions() {
		return new Page<AnalysisSubmission>() {
			@Override
			public int getTotalPages() {
				return 15;
			}

			@Override
			public long getTotalElements() {
				return 150;
			}

			@Override
			public int getNumber() {
				return 0;
			}

			@Override
			public int getSize() {
				return 0;
			}

			@Override
			public int getNumberOfElements() {
				return 0;
			}

			@Override
			public List<AnalysisSubmission> getContent() {
				return ImmutableList.of();
			}

			@Override
			public boolean hasContent() {
				return false;
			}

			@Override
			public Sort getSort() {
				return null;
			}

			@Override
			public boolean isFirst() {
				return false;
			}

			@Override
			public boolean isLast() {
				return false;
			}

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public boolean hasPrevious() {
				return false;
			}

			@Override
			public Pageable nextPageable() {
				return null;
			}

			@Override
			public Pageable previousPageable() {
				return null;
			}

			@Override
			public Iterator<AnalysisSubmission> iterator() {
				return null;
			}
		};
	}
}
