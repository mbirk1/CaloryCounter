export interface DuplicateAnalysisJobStatus {
  jobId: string
  source: string
  state: 'RUNNING' | 'COMPLETED' | 'FAILED'
  clustersFound: number
  errorMessage?: string
  startedAt: string
  finishedAt?: string
}
