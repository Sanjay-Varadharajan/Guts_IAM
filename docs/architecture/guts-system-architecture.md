flowchart TD

%% CLIENT
A[Client / Web App / API User]

%% PROXY
B[GUTS-PROXY - API Gateway]

%% IAM
C[GUTS-IAM - Auth Service]

%% REDIS
D[(Redis Cache)]

%% MYSQL
E[(MySQL Database)]

%% FLOW
A -->|1. Request + API Key| B

B -->|2. Validate API Key| D
D -->|Cache Lookup| B

B -->|3. Forward Request| C

C -->|4. Authenticate User| D
D -->|Session / Roles| C

C -->|5. Fetch Data| E
E -->|User / Roles| C

C -->|6. Auth Response| B

B -->|7. Response| A

%% LOGGING
B -->|Logs| E
