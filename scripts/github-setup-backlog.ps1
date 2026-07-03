# GitHub backlog bootstrap - Civs + RPG Server
$ErrorActionPreference = "Stop"
$gh = "C:\Users\Danie\tools\gh\bin\gh.exe"

function Ensure-Label {
    param([string]$Repo, [string]$Name, [string]$Color, [string]$Description = "")
    $null = & $gh label create $Name --repo $Repo --color $Color --description $Description --force 2>&1
}

function Ensure-Milestone {
    param([string]$Repo, [string]$Title, [string]$Description, [string]$DueOn = "")
    $existing = & $gh api "repos/$Repo/milestones?state=all" --jq ".[].title"
    if ($existing -contains $Title) { return }
    $payload = @{ title = $Title; description = $Description; state = "open" }
    if ($DueOn) { $payload.due_on = $DueOn }
    ($payload | ConvertTo-Json -Compress) | & $gh api "repos/$Repo/milestones" -X POST --input - | Out-Null
    Write-Host "  + milestone $Title on $Repo"
}

function New-Issue {
    param(
        [string]$Repo,
        [string]$Title,
        [string]$Body,
        [string]$LabelCsv,
        [string]$Milestone,
        [switch]$Close
    )
    $issueUrl = & $gh issue create --repo $Repo --title $Title --body $Body --label $LabelCsv --milestone $Milestone
    $num = ($issueUrl -split '/')[-1]
    if ($Close) {
        & $gh issue close $num --repo $Repo --comment "Concluido no Sprint 1 (brain: SPRINT-1-STATUS.md)." | Out-Null
        Write-Host "  [closed] issue $num - $Title"
    } else {
        Write-Host "  [open]   issue $num - $Title"
    }
}

$repos = @("Daniel730/Civs", "Daniel730/civs-quests")
$labelDefs = @(
    @("sprint-1", "0E8A16", "Sprint 1"),
    @("sprint-2", "1D76DB", "Sprint 2"),
    @("sprint-3", "5319E7", "Sprint 3"),
    @("sprint-4", "B60205", "Sprint 4+"),
    @("civs", "FBCA04", "Civs Custom plugin"),
    @("rpg", "D93F0B", "RPG Server plugin"),
    @("P0", "B60205", "Critical priority"),
    @("P1", "E99695", "High priority"),
    @("P2", "C5DEF5", "Medium priority"),
    @("integration", "006B75", "Plugin integration"),
    @("enhancement", "A2EEEF", "New feature")
)

Write-Host "=== Labels ==="
foreach ($repo in $repos) {
    foreach ($l in $labelDefs) {
        Ensure-Label -Repo $repo -Name $l[0] -Color $l[1] -Description $l[2]
    }
    Write-Host "  labels OK on $repo"
}

Write-Host ""
Write-Host "=== Milestones ==="
$msDefs = @(
    @("Sprint 1", "GainExpEvent, objective registry, hooks (DONE)", "2026-06-30T23:59:59Z"),
    @("Sprint 2", "Civs StatManager + Auction BIN; RPG SkillTree + journal", "2026-07-31T23:59:59Z"),
    @("Sprint 3", "Turrets, custom mobs, PAPI completo", "2026-08-31T23:59:59Z"),
    @("Sprint 4", "Unificar Civs tutorials + RPG quests", "2026-09-30T23:59:59Z")
)
foreach ($repo in $repos) {
    foreach ($m in $msDefs) {
        Ensure-Milestone -Repo $repo -Title $m[0] -Description $m[1] -DueOn $m[2]
    }
}

Write-Host ""
Write-Host "=== Civs issues ==="
$civs = "Daniel730/Civs"

New-Issue -Repo $civs -Title "CIVS-001: Wire GainExpEvent from Civilian.awardSkill / addSkillXp" -Body "Fire cancellable GainExpEvent before Civs-internal skill XP apply." -LabelCsv "civs,sprint-1,P0,enhancement" -Milestone "Sprint 1" -Close
New-Issue -Repo $civs -Title "CIVS-002: Central SkillListener" -Body "Mining, farming, fishing, combat in SkillListener.java." -LabelCsv "civs,sprint-1,P0,enhancement" -Milestone "Sprint 1" -Close
New-Issue -Repo $civs -Title "CIVS-003: PAPI civs_skill placeholders" -Body "PlaceHook skill level and xp placeholders." -LabelCsv "civs,sprint-1,P0,integration" -Milestone "Sprint 1" -Close
New-Issue -Repo $civs -Title "CIVS-004: Public Civilian.addSkillXp" -Body "bonusExp persistence in civilian YAML." -LabelCsv "civs,sprint-1,P0,enhancement" -Milestone "Sprint 1" -Close
New-Issue -Repo $civs -Title "CIVS-005: Native chest shop MVP (deferred)" -Body "Deferred. Server uses ChestShop." -LabelCsv "civs,sprint-1,P2" -Milestone "Sprint 1" -Close
New-Issue -Repo $civs -Title "CIVS-006: StatManager territorial stat modifiers" -Body "Civs StatManager for town/region perks. Blocks RPG territorial perks." -LabelCsv "civs,sprint-2,P0,enhancement" -Milestone "Sprint 2"
New-Issue -Repo $civs -Title "CIVS-007: Auction house BIN native" -Body "AuctionManager, CustomMenu GUI, Vault, AuctionListEvent, AuctionPurchaseEvent." -LabelCsv "civs,sprint-2,P0,enhancement" -Milestone "Sprint 2"
New-Issue -Repo $civs -Title "CIVS-008: Wire SpellPreCastEvent" -Body "Fire event before spell cast for RPG quests." -LabelCsv "civs,sprint-2,P1,enhancement" -Milestone "Sprint 2"
New-Issue -Repo $civs -Title "CIVS-009: Turrets and town shields" -Body "KingdomX patterns. Region effect plus YAML menus." -LabelCsv "civs,sprint-3,P1,enhancement" -Milestone "Sprint 3"
New-Issue -Repo $civs -Title "CIVS-010: Custom mob YAML and CustomMobKillEvent" -Body "MythicMobs-style YAML without MythicMobs runtime." -LabelCsv "civs,sprint-3,P2,enhancement" -Milestone "Sprint 3"

Write-Host ""
Write-Host "=== RPG issues ==="
$rpg = "Daniel730/civs-quests"

New-Issue -Repo $rpg -Title "RPG-001: ObjectiveTypeRegistry" -Body "Sprint 1 DONE." -LabelCsv "rpg,sprint-1,P0,enhancement" -Milestone "Sprint 1" -Close
New-Issue -Repo $rpg -Title "RPG-002: CivsSkillHook cancelled" -Body "Keep AuraSkillsHook." -LabelCsv "rpg,sprint-1" -Milestone "Sprint 1" -Close
New-Issue -Repo $rpg -Title "RPG-003: Core objective types" -Body "kill_mob, mine_block, earn_money, build_region, skill_level." -LabelCsv "rpg,sprint-1,P0,enhancement" -Milestone "Sprint 1" -Close
New-Issue -Repo $rpg -Title "RPG-004: Quest chain requires" -Body "QuestManager.meetsRequirements." -LabelCsv "rpg,sprint-1,P0,enhancement" -Milestone "Sprint 1" -Close
New-Issue -Repo $rpg -Title "RPG-005: RewardExecutor" -Body "Vault, AuraSkills XP, LuckPerms." -LabelCsv "rpg,sprint-1,P0,enhancement" -Milestone "Sprint 1" -Close
New-Issue -Repo $rpg -Title "RPG-006: ChestShopHook" -Body "shop_buy sell revenue via TransactionEvent." -LabelCsv "rpg,sprint-1,P0,integration" -Milestone "Sprint 1" -Close
New-Issue -Repo $rpg -Title "RPG-007: EssentialsHook" -Body "balance_min, kit and warp rewards." -LabelCsv "rpg,sprint-1,P1,integration" -Milestone "Sprint 1" -Close
New-Issue -Repo $rpg -Title "RPG-008: InteractiveBooksHook" -Body "lore-book on quest start." -LabelCsv "rpg,sprint-1,P1,integration" -Milestone "Sprint 1" -Close
New-Issue -Repo $rpg -Title "RPG-009: VeinMiner objective optional" -Body "Optional P2 vein_mine objective." -LabelCsv "rpg,sprint-2,P2,integration" -Milestone "Sprint 2"
New-Issue -Repo $rpg -Title "RPG-010: Civs territorial objectives" -Body "civs_skill_xp and civs_skill_level. Implemented locally, needs PR." -LabelCsv "rpg,sprint-2,P1,integration" -Milestone "Sprint 2" -Close
New-Issue -Repo $rpg -Title "RPG-011: SkillTreeManager and StatModifier perks" -Body "YAML perks, unlocked-perks profile, /rpg perks." -LabelCsv "rpg,sprint-2,P1,enhancement" -Milestone "Sprint 2"
New-Issue -Repo $rpg -Title "RPG-012: Quest journal GUI" -Body "In-game /rpg journal with progress." -LabelCsv "rpg,sprint-2,P1,enhancement" -Milestone "Sprint 2"
New-Issue -Repo $rpg -Title "RPG-013: PAPI rpg_quest_progress" -Body "Expand RpgPlaceholderExpansion." -LabelCsv "rpg,sprint-2,P1,integration" -Milestone "Sprint 2"
New-Issue -Repo $rpg -Title "RPG-014: reload ChestShop re-register" -Body "Known gap on /rpg reload." -LabelCsv "rpg,sprint-2,P2,enhancement" -Milestone "Sprint 2"
New-Issue -Repo $rpg -Title "RPG-015: Auction quest objectives" -Body "Blocked on CIVS-007. auction_list and auction_buy." -LabelCsv "rpg,sprint-2,P1,integration" -Milestone "Sprint 2"

Write-Host ""
Write-Host "=== Close Sprint 1 milestone ==="
foreach ($repo in $repos) {
    $num = & $gh api "repos/$repo/milestones?state=open" --jq '.[] | select(.title=="Sprint 1") | .number'
    if ($num) {
        & $gh api "repos/$repo/milestones/$num" -X PATCH -f state=closed | Out-Null
        Write-Host "  Closed Sprint 1 on $repo milestone $num"
    }
}

Write-Host ""
Write-Host "Done."
Write-Host "Civs: https://github.com/Daniel730/Civs/issues"
Write-Host "RPG:  https://github.com/Daniel730/civs-quests/issues"
