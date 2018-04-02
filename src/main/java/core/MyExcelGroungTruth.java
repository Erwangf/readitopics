package core;

import io.ExportResults;

/**
 * 
 * @author cgravier
 *
 */
public class MyExcelGroungTruth {

	private String folder_best;
	private ExportResults export;

	public MyExcelGroungTruth(String folder_best, ExportResults export) {
		super();
		this.folder_best = folder_best;
		this.export = export;
	}

	public MyExcelGroungTruth() {
		super();
		this.folder_best = null;
		this.export = null;
	}

	@Override
	public String toString() {
		return "MyExcelGroungTruth [folder_best=" + folder_best + ", export=" + export + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((export == null) ? 0 : export.hashCode());
		result = prime * result + ((folder_best == null) ? 0 : folder_best.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MyExcelGroungTruth other = (MyExcelGroungTruth) obj;
		if (export == null) {
			if (other.export != null)
				return false;
		} else if (!export.equals(other.export))
			return false;
		if (folder_best == null) {
			if (other.folder_best != null)
				return false;
		} else if (!folder_best.equals(other.folder_best))
			return false;
		return true;
	}

	public String getFolder_best() {
		return folder_best;
	}

	public void setFolder_best(String folder_best) {
		this.folder_best = folder_best;
	}

	public ExportResults getExport() {
		return export;
	}

	public void setExport(ExportResults export) {
		this.export = export;
	}

}
