export interface ImportJobStatus {
  jobId: string
  state: 'RUNNING' | 'COMPLETED' | 'FAILED'
  totalBytes: number
  bytesRead: number
  processedRows: number
  importedCount: number
  skippedCount: number
  errorCount: number
  startedAt: string
  finishedAt?: string
}
