import re
import urllib


def game_results(agentName):
    text_match = '([\w.&\ -]+)'
    number_match = '(?:<font color=red>)?([-+]?[0-9]*\.?[0-9]*)(?:</font>)?'

    exclude_games = []

    agent_results = {}
    scores = []
    positions = []
    differences = []

    # Download the results from the ECS TAC server


    gameNum = 0
    average = 0
    totalScore = 0
    for game in range(613,657):
    
        if game not in exclude_games:
    
            f = urllib.urlopen("http://tac.ecs.soton.ac.uk:8080/history/"+str(game)+"/")
            s = f.read()
            f.close()
    
            if s.count('HTTP ERROR')==0:
            
                #print "Reading game " + str(game)
                        
                r = re.findall(r'(<tr><td>'+text_match+'</td><td>'+number_match+'</td><td>'+number_match+'</td><td>'+number_match+'</td>)', s)
                i = 0
                for tuple in r:
                    i+=1
                
                
                
                    agent_name = tuple[1]
                    agent_utility = tuple[2]
                    agent_cost = tuple[3]
                    agent_score = tuple[4]

                
                
                    if i == 1:
                        topScorer = float(agent_score)
                
                    if agent_name == agentName: 
                        gameNum+=1
                        totalScore += float(agent_score)
                        average = float(totalScore)/float(gameNum)
                        difference = topScorer - float(agent_score)
                        positions.append(int(i))
                        differences.append(float(difference))
                        scores.append(float(agent_score))
                        print agentName +" " + str(gameNum) + " " + str(game) + " " + agent_score + " " + str(i) + " " + str(difference) + " " + str(average)
                    
                    
                    
                    
                    if agent_name not in agent_results:
            
                        agent_results[agent_name] = []
            
                    agent_results[agent_name].append( (game, float(agent_score)) )
                
            else:
            
                print "Skipping game " + str(game)   
           
        else:
            
            print "Skipping game " + str(game)
    meanPositions = float(float(sum(positions))/float(len(positions)))
    meanDifferences = float(float(sum(differences))/float(len(differences)))
    print str(meanPositions) + " " + str(meanDifferences)

agents = ["tic-tac","rex-ready","miniagent","TheGreaterFool","Penelope","bucephalus","AgentElman","Cortana","westridge","AbsMTree","aaaaaaaa","AgentSmith","HAL","PostTraumaticAgent","Hermes","Avengers","Desolution","Wukong","SexyAgent","NAMP","CrazySaver","Agent-HN"]
for anAgent in agents:
    game_results(anAgent)
