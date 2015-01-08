import re
import urllib

text_match = '([\w.&\ -]+)'
number_match = '(?:<font color=red>)?([-+]?[0-9]*\.?[0-9]*)(?:</font>)?'

exclude_games = []

agent_results = {}

agent_nams = {"AgentElman"}
# Download the results from the ECS TAC server

scores = {}
positions = {}
differences = {}

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
                agentName = 'AgentElman'
                
                
                agent_name = tuple[1]
                agent_utility = tuple[2]
                agent_cost = tuple[3]
                agent_score = tuple[4]
                
                if i == 1:
                    topScorer = float(agent_score)
                
                if agent_name == agentName: 
                    difference = topScorer - float(agent_score)
                    print agent_score + " " + str(i) + " " + str(difference)
                    
                    
                    
                if agent_name not in agent_results:
            
                    agent_results[agent_name] = []
            
                agent_results[agent_name].append( (game, float(agent_score)) )
                
        else:
            
            print "Skipping game " + str(game)   
           
    else:
            
        print "Skipping game " + str(game)

# Calculate the mean score

agent_means = []

for agent,scores in agent_results.items():
    
    total = 0
    number = 0
    
    for game,result in scores:
        total = total + result
        number = number + 1
        
    agent_means.append( ( agent, round(float(total)/float(number),0), number ) )
    
agent_means = sorted(agent_means, key=lambda value: value[1], reverse=True)
    
print
print "MEAN SCORES"
print

count = 1

for agent,scores,number in agent_means:
    
    print str(count) + " \t " + agent + " \t " + str(scores) + " \t " + str(number)
    
    count = count + 1